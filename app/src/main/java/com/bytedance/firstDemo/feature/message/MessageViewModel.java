package com.bytedance.firstDemo.feature.message;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.model.FriendModel;
import com.bytedance.firstDemo.data.model.JsonMessage;
import com.bytedance.firstDemo.data.model.MessageModel;
import com.bytedance.firstDemo.data.model.SearchConversationResult;
import com.bytedance.firstDemo.data.model.SearchMessageResult;
import com.bytedance.firstDemo.data.model.SystemMessageModel;
import com.bytedance.firstDemo.data.repository.FriendRepository;
import com.bytedance.firstDemo.data.repository.MessageRepository;
import com.bytedance.firstDemo.data.repository.SystemMessageRepository;
import com.bytedance.firstDemo.data.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * ViewModel：负责消息会话列表（支持系统消息）、聊天记录、分页加载、好友信息同步等业务逻辑。
 * 可从好友消息表 message 与系统消息表 system_message 混排展示。
 */
public class MessageViewModel extends ViewModel {

    // ---- 状态枚举（加载、成功、空、错误） ----
    public enum LoadState {
        SKELETON,
        LOADING,
        SUCCESS,
        EMPTY,
        ERROR,
        TIMEOUT
    }


    // ---- LiveData：会话列表 & 聊天内容 ----

    private final MutableLiveData<List<MessageModel>> sessionsLive = new MutableLiveData<>();
    public LiveData<List<MessageModel>> sessions = sessionsLive;

    private final MutableLiveData<List<MessageModel>> chatMessagesLive = new MutableLiveData<>();
    public LiveData<List<MessageModel>> chatMessages = chatMessagesLive;

    // 加载状态（用于空态页 / 错误页 / skeleton）
    public final MutableLiveData<LoadState> loadState = new MutableLiveData<>();

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;
    // ---- Repository ----

    private MessageRepository messageRepo;
    private FriendRepository friendRepo;
    private UserRepository userRepo;
    private SystemMessageRepository systemRepo;

    // ===== 初始化相关方法 =====

    /**
     * 初始化 ViewModel：构造 Repository。
     */
    public void init(Context ctx) {
        messageRepo = new MessageRepository(ctx);
        friendRepo = new FriendRepository(ctx);
        userRepo = new UserRepository(ctx);
        systemRepo = new SystemMessageRepository(ctx);
    }

    // ===== 会话列表（分页与混排系统消息） =====

    /**
     * 内部工具：构造“会话列表数据”
     * 1）从 message 表取每个会话最后一条
     * 2）补充好友备注 / 头像 / 未读数
     * 3）把最新一条系统互动消息混排到最前面
     */
    private List<MessageModel> buildSessionList() {

        // 1. 获取所有会话的最后一条消息
        List<MessageModel> list = messageRepo.getAllSessionsLastMessage();

        // 2. 为好友会话补充昵称 / 头像 / 未读数
        if (list != null) {
            // 划分置顶和非置顶的好友消息
            List<MessageModel> pinnedFriends = new ArrayList<>();
            List<MessageModel> nonPinnedFriends = new ArrayList<>();

            for (MessageModel m : list) {
                if (m.getType() == MessageModel.TYPE_FRIEND) {
                    int friendId = -1;
                    try {
                        friendId = Integer.parseInt(m.sessionId.replace("friend_", ""));
                    } catch (Exception ignored) {}

                    if (friendId != -1) {
                        FriendModel f = friendRepo.getFriendById(friendId);
                        if (f != null&& f.isHidden == 0) {
                            // 昵称或备注
                            m.setFriendName(f.remark != null && !f.remark.isEmpty() ? f.remark : f.nickname);
                            m.setFriendAvatar(f.avatar);
                            m.unread = f.unread;

                            // 根据置顶状态进行分组
                            if (f.isPinned == 1) {
                                m.setPinned(true); // 确保 MessageModel 的 isPinned 字段正确设置
                                pinnedFriends.add(m);  // 置顶好友
                                Log.d("FriendRepository", "Pinned status updated: " + friendId + " -> " + f.isPinned);
                            } else {
                                m.setPinned(false);  // 确保 MessageModel 的 isPinned 字段正确设置
                                nonPinnedFriends.add(m);  // 非置顶好友
                            }
                        }
                    }
                }
            }

            // 3. 置顶好友消息放在最前面
            Collections.sort(nonPinnedFriends, (msg1, msg2) -> {
                // 按时间排序未置顶的好友消息（降序）
                return msg2.getTime().compareTo(msg1.getTime());
            });

            // 将置顶的好友消息和未置顶的好友消息合并
            list.clear();
            list.addAll(pinnedFriends);  // 先添加置顶的好友消息
            list.addAll(nonPinnedFriends);  // 后添加未置顶的好友消息
        }

        // 4. 获取系统消息（互动消息当前只有一种类型），混排到最前面
        SystemMessageModel sys = systemRepo.getLatestMessage(SystemMessageModel.TYPE_INTERACTION);
        if (sys != null) {
            int icon = R.drawable.ic_system_default;
            MessageModel systemMsg = MessageModel.fromSystem(sys, icon);
            if (list != null) {
                list.add(0, systemMsg);  // 系统消息放到最前面
            }
        }

        return list;
    }



    /**
     * 原始刷新接口：直接加载会话列表（本地 DB），不模拟网络。
     */
    // MessageViewModel.java
    public void loadSessions() {
        // 获取并排序好友列表（已置顶的好友排在最前面）
        List<MessageModel> list = buildSessionList(); // 获取会话列表
        sessionsLive.setValue(list); // 更新 LiveData
        Log.d("MessageViewModel", "Loaded sessions: " + list.size());
    }




    /**
     * 新增：模拟“网络加载会话列表”
     * - 先设置状态为 LOADING
     * - 随机 sleep 一段时间
     * - 随机 30% 失败 -> ERROR
     * - 成功则根据 list 是否为空设置为 SUCCESS / EMPTY
     */
    public void simulateNetworkLoadSessions() {
        loadState.postValue(LoadState.LOADING);

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (Exception ignore) {}

            if (Math.random() < 0.3) {
                loadState.postValue(LoadState.ERROR);
                return;
            }

            List<MessageModel> list = buildSessionList();
            sessionsLive.postValue(list);

            if (list == null || list.isEmpty()) {
                loadState.postValue(LoadState.EMPTY);
            } else {
                loadState.postValue(LoadState.SUCCESS);
            }

            timeoutHandler.removeCallbacks(timeoutRunnable);

        }).start();
    }

    public void loadSessionsWithSkeleton() {
        loadState.setValue(LoadState.SKELETON);

        simulateNetworkLoadSessions();

        startTimeoutCheck();
    }

    private void startTimeoutCheck() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        timeoutRunnable = () -> {
            LoadState cur = loadState.getValue();
            if (cur != LoadState.SUCCESS && cur != LoadState.EMPTY) {
                loadState.setValue(LoadState.TIMEOUT);
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, 5000);  // 5 秒超时
    }

    public void addFriend(String nickname) {
        if (nickname == null || nickname.isEmpty()) return;

        friendRepo.addFriend(nickname);
        loadSessions(); // 刷新会话列表
    }




    // ===== 聊天记录相关方法 =====

    /**
     * 加载某个会话全部聊天记录（好友或群聊）。
     */
    public void loadChatMessages(String sessionId) {
        List<MessageModel> msgs = messageRepo.getMessagesBySession(sessionId);
        chatMessagesLive.setValue(msgs);
    }

    // ===== 写入消息（好友/群聊） =====

    /**
     * 写入一条好友消息，并刷新会话列表。
     */
    public void addFriendMessage(Context ctx, int friendId, String friendName, boolean isMe, String content) {

        String sender = isMe ? userRepo.getCurrentNickname(ctx) : friendName;
        String time = now();
        String sessionId = "friend_" + friendId;

        JsonMessage jm = new JsonMessage();
        jm.sessionId = sessionId;
        jm.type = 1;      // 会话类型：好友
        jm.msgType = 1;   // 内容类型：文本
        jm.senderName = sender;
        jm.content = content;
        jm.time = time;
        jm.isMe = isMe;

        if (!isMe) friendRepo.increaseUnread(friendId);

        messageRepo.insertMessage(jm);

        String lastMsg = (isMe ? "我：" : sender + "：") + content;
        friendRepo.updateFriendLastMessage(friendName, lastMsg, time);

        // 刷新会话列表
        loadSessions();
    }

    /**
     * 写入一条群聊消息。
     */
    public void addGroupMessage(Context ctx, String groupName, boolean isMe, String inputSender, String content) {

        String sender = isMe ? userRepo.getCurrentNickname(ctx) : inputSender;
        String time = now();
        String sessionId = "group_" + groupName;

        JsonMessage jm = new JsonMessage();
        jm.sessionId = sessionId;
        jm.type = 2;
        jm.senderName = sender;
        jm.content = content;
        jm.time = time;
        jm.isMe = isMe;
        jm.unread = isMe ? 0 : 1;
        jm.type = 2;
        jm.msgType = 1;   // 文本

        messageRepo.insertMessage(jm);
    }

    /**
     * ViewModel 层：添加一条互动系统消息（自动刷新会话列表）
     */
    public void addInteractionMessage(String commentContent) {
        String time = now();
        systemRepo.insertInteractionMessage("", "", commentContent, time);
        loadSessions();
    }

    public void addChatMessage(String sessionId, String content) {
        JsonMessage jm = new JsonMessage();
        jm.sessionId = sessionId;
        jm.type = MessageModel.TYPE_FRIEND;
        jm.msgType = 1;  // 文本消息
        jm.senderName = "系";
        jm.content = content;
        jm.time = now();
        jm.unread = 0;
        jm.isMe = true;

        messageRepo.insertMessage(jm);
        loadSessions();
    }


    /**
     * 获取全部系统消息（用于展示全部系统消息）。
     */
    public List<SystemMessageModel> getAllSystemMessages() {
        return systemRepo.getAllMessages(SystemMessageModel.TYPE_INTERACTION);
    }


    // ===== 工具方法 =====

    /**
     * 获取当前系统时间。
     */
    private String now() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 清空系统消息未读（全部互动消息）。
     */
    public void clearSystemUnread() {
        systemRepo.clearUnread(SystemMessageModel.TYPE_INTERACTION);
    }

    // ===== 清空/更新好友关系相关 =====

    public void clearAllMessages() {
        messageRepo.clearMessages();
        loadSessions();
    }

    public void clearAllFriends() {
        friendRepo.clearFriends();
        loadSessions();
    }

    public void clearUnreadForFriend(int friendId) {
        friendRepo.clearUnread(friendId);
        loadSessions();
    }

    public void updateRemark(int friendId, String remark) {
        friendRepo.updateRemark(friendId, remark);
        loadSessions();
    }

    /**
     * 点击会话项时调用：
     * 1. 根据 sessionId 自动解析出 friendId
     * 2. 清空该好友的未读数
     * 3. 刷新会话列表 UI
     */
    public void onSessionClicked(MessageModel model) {
        if (model.getType() != MessageModel.TYPE_FRIEND) {
            return; // 非好友会话不处理
        }

        // sessionId 格式：friend_3
        int friendId = -1;
        try {
            friendId = Integer.parseInt(model.sessionId.replace("friend_", ""));
        } catch (Exception ignored) {
        }

        if (friendId != -1) {
            // 清空好友未读
            clearUnreadForFriend(friendId);
        }

        // 刷新会话首页
        loadSessions();
    }

    // ==================== 搜索功能：新增 LiveData ====================

    // 一级搜索结果（按会话分组）
    private final MutableLiveData<List<SearchConversationResult>> searchResultLive = new MutableLiveData<>();
    public LiveData<List<SearchConversationResult>> searchResults = searchResultLive;

    // 二级搜索结果（某个会话内部所有匹配消息）
    private final MutableLiveData<List<SearchMessageResult>> _sessionSearchResults =
            new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<SearchMessageResult>> sessionSearchResults = _sessionSearchResults;


// ==================== 搜索功能 ====================

    /**
     * 一级搜索：搜索所有会话中的匹配消息，然后按 sessionId 分组
     */
    public void searchAllSessions(String keyword) {
        new Thread(() -> {
            List<MessageModel> rawList = messageRepo.searchAllMessages(keyword); // ★ Repository 查询

            Map<String, List<MessageModel>> grouped = new HashMap<>();

            for (MessageModel m : rawList) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    grouped
                            .computeIfAbsent(m.sessionId, k -> new ArrayList<>())
                            .add(m);
                }
            }

            List<SearchConversationResult> result = new ArrayList<>();

            for (Map.Entry<String, List<MessageModel>> entry : grouped.entrySet()) {
                String sessionId = entry.getKey();
                List<MessageModel> list = entry.getValue();

                int count = list.size();

                MessageModel latest = list.get(0);

                String name = "";
                String avatar = "";

                if (latest.getType() == MessageModel.TYPE_FRIEND) {
                    int friendId = Integer.parseInt(latest.sessionId.replace("friend_", ""));
                    FriendModel f = friendRepo.getFriendById(friendId);
                    if (f != null) {
                        name = (f.remark != null && !f.remark.isEmpty()) ? f.remark : f.nickname;
                        avatar = f.avatar;
                    }
                } else if (latest.getType() == MessageModel.TYPE_GROUP) {
                    name = latest.sender;
                }

                SearchConversationResult item = new SearchConversationResult(
                        sessionId,
                        name,
                        avatar,
                        count,
                        latest
                );

                result.add(item);
            }

            searchResultLive.postValue(result); // ★ 子线程必须使用 postValue()

        }).start();
    }

    /**
     * 二级搜索：搜索某个会话内部匹配消息
     */
    public void searchMessagesInSession(String sessionId, String keyword) {
        new Thread(() -> {

            List<MessageModel> rawList =
                    messageRepo.searchMessagesInSession(sessionId, keyword);

            List<SearchMessageResult> result = new ArrayList<>();

            for (MessageModel m : rawList) {
                result.add(new SearchMessageResult(
                        m.id,
                        m.content,
                        m.getTime(),
                        m.isMe
                ));
            }

            _sessionSearchResults.postValue(result);  // ★ 修复：必须用 postValue()

        }).start();
    }

    /**
     * 二级搜索：清空结果（一定是 UI 线程调用）
     */
    public void clearSessionSearchResult() {
        _sessionSearchResults.setValue(new ArrayList<>()); // ✔ 主线程使用 setValue
    }




// ==================== 搜索部分结束 ====================

    public void insertExternalMessage(JsonMessage jm) {

        // 写入数据库
        messageRepo.insertMessage(jm);

        // sessionId = "friend_xxx"
        if (jm.sessionId != null && jm.sessionId.startsWith("friend_")) {

            try {
                int friendId = Integer.parseInt(jm.sessionId.replace("friend_", ""));
                friendRepo.increaseUnread(friendId);
            } catch (Exception ignored) {}
        }

        // 刷新消息列表
        loadSessions();
    }

    // ===== 清空所有未读（好友 + 系统） =====
    public void clearAllUnread() {

        // 1. 清空所有好友未读
        List<FriendModel> list = friendRepo.getAllFriends();
        if (list != null) {
            for (FriendModel f : list) {
                friendRepo.clearUnread(f.friendId);
            }
        }

        // 2. 清空系统消息未读
        systemRepo.clearUnread(SystemMessageModel.TYPE_INTERACTION);

        // 3. 刷新会话列表（更新 UI）
        loadSessions();
    }

}

// ======================== ChatViewModel.java ========================
package com.bytedance.firstDemo.feature.chat;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bytedance.firstDemo.data.model.JsonMessage;
import com.bytedance.firstDemo.data.model.MessageModel;
import com.bytedance.firstDemo.data.repository.FriendRepository;
import com.bytedance.firstDemo.data.repository.MessageRepository;
import com.bytedance.firstDemo.data.repository.UserRepository;
import com.bytedance.firstDemo.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    /** 正序消息列表（旧→新） */
    public MutableLiveData<List<MessageModel>> chatList = new MutableLiveData<>();

    private final MessageRepository messageRepo;
    private final FriendRepository friendRepo;
    private final UserRepository userRepo;

    private String sessionId;
    private int friendId;
    private String friendAvatarPath;
    private String myAvatarPath;

    private int pageIndex = 0;
    private final int pageSize = 20;
    private boolean noMoreData = false;

    public ChatViewModel(@NonNull Application app) {
        super(app);
        messageRepo = new MessageRepository(app);
        friendRepo = new FriendRepository(app);
        userRepo = new UserRepository(app);
    }

    // ===== 初始化 =====
    public void initChat(int friendId, String sessionId, String friendAvatar) {
        this.friendId = friendId;
        this.sessionId = sessionId;
        this.friendAvatarPath = friendAvatar;

        this.myAvatarPath = userRepo.getUserAvatar(getApplication());

        friendRepo.clearUnread(friendId);

        loadFirstPage();
    }

    // ===== 加载第一页 =====
    public void loadFirstPage() {

        pageIndex = 0;
        noMoreData = false;

        List<MessageModel> list =
                messageRepo.getMessagesBySession(sessionId, pageSize, 0);

        Collections.reverse(list);

        for (MessageModel m : list) fillAvatar(m);

        fillTimestamp(list);

        chatList.setValue(list);
    }

    // ===== 为列表插入时间提示项（修复重复时间条问题）=====
    private void fillTimestamp(List<MessageModel> list) {
        if (list == null || list.isEmpty()) return;

        List<MessageModel> result = new ArrayList<>();

        MessageModel prevMsg = null; // 上一条“真正的消息”
        long prevTs = 0L;

        for (MessageModel m : list) {

            if (m == null) continue;

            // 1️⃣ 旧的时间条本身不参与重新计算，直接跳过
            if (m.showTime) {
                continue;
            }

            long ts = TimeUtils.parse(m.getTime());

            // 2️⃣ 时间戳无效（例如 0），不要插时间条，直接加消息
            if (ts <= 0) {
                result.add(m);
                prevMsg = m;
                continue;
            }

            boolean needShowTime = false;

            if (prevMsg == null) {
                // 第一个有效消息，一定要显示一次时间
                needShowTime = true;
            } else {
                long diff = ts - prevTs;
                // 这里可以按需求调：例如 5 分钟、10 分钟、1 分钟……
                // 当前逻辑：超过 1 分钟才插下一条时间提示
                if (diff > 60 * 1000) {
                    needShowTime = true;
                }
            }

            if (needShowTime) {
                MessageModel timeModel = new MessageModel();
                timeModel.showTime = true;
                timeModel.timeText = TimeUtils.formatChatTime(ts);
                result.add(timeModel);
            }

            // 最后真正把这条消息加入结果
            result.add(m);
            prevMsg = m;
            prevTs = ts;
        }

        // 用结果列表替换原来的列表
        list.clear();
        list.addAll(result);
    }


    // ===== 上滑加载更多 =====
    public void loadMore() {
        if (noMoreData) {
            // 已经没有更多数据了，直接把当前列表再发一遍，方便 UI 停止刷新
            List<MessageModel> cur = chatList.getValue();
            chatList.postValue(cur);
            return;
        }

        pageIndex++;
        int offset = pageIndex * pageSize;

        List<MessageModel> more =
                messageRepo.getMessagesBySession(sessionId, pageSize, offset);

        if (more.isEmpty()) {
            // 没有更多数据了，标记，并把当前列表再发一遍，通知 UI 停止刷新
            noMoreData = true;
            List<MessageModel> cur = chatList.getValue();
            chatList.postValue(cur);
            return;
        }

        Collections.reverse(more);

        for (MessageModel m : more) fillAvatar(m);

        List<MessageModel> cur = chatList.getValue();
        if (cur == null) cur = new ArrayList<>();

        // 新加载的一页插在“最前面”
        cur.addAll(0, more);

        // 重新插入时间条
        fillTimestamp(cur);

        chatList.postValue(cur);
    }


    // ===== 发送文字消息 =====
    public void sendMessage(String content) {
        if (content == null || content.trim().isEmpty()) return;

        JsonMessage jm = new JsonMessage();
        jm.sessionId = sessionId;
        jm.type = 1;
        jm.msgType = 1;
        jm.senderName = userRepo.getCurrentNickname(getApplication());
        jm.content = content;
        jm.time = TimeUtils.now();
        jm.unread = 0;
        jm.isMe = true;

        messageRepo.insertMessage(jm);

        reloadLatestPage();
    }

    // ===== 图像消息 =====
    public void sendImageMessage() {
        JsonMessage jm = new JsonMessage();
        jm.sessionId = sessionId;
        jm.type = 1;
        jm.msgType = 2;
        jm.senderName = userRepo.getCurrentNickname(getApplication());
        jm.content = "[图片]";
        jm.time = TimeUtils.now();
        jm.unread = 0;
        jm.isMe = true;
        jm.imagePath = null;

        messageRepo.insertMessage(jm);
        reloadLatestPage();
    }

    // ===== 运营卡片 =====
    public void sendOperateMessage(String content, String actionText) {
        JsonMessage jm = new JsonMessage();
        jm.sessionId = sessionId;
        jm.type = 1;
        jm.msgType = 3;
        jm.senderName = userRepo.getCurrentNickname(getApplication());
        jm.content = content;
        jm.actionText = actionText;
        jm.time = TimeUtils.now();
        jm.unread = 0;
        jm.isMe = true;

        messageRepo.insertMessage(jm);
        reloadLatestPage();
    }

    // ===== 头像补齐 =====
    private void fillAvatar(MessageModel m) {
        if (m.isMe) {
            m.avatarPath = myAvatarPath;
        } else {
            m.avatarPath = friendAvatarPath;
        }
    }

    // ===== 重新加载最新页 =====
    private void reloadLatestPage() {

        List<MessageModel> list =
                messageRepo.getMessagesBySession(sessionId, pageSize, 0);

        Collections.reverse(list);

        for (MessageModel m : list) fillAvatar(m);

        fillTimestamp(list);

        chatList.setValue(list);
    }
}

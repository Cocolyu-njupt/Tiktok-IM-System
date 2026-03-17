package com.bytedance.firstDemo.feature.message;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.core.metrics.MetricsCenter;
import com.bytedance.firstDemo.data.model.FriendModel;
import com.bytedance.firstDemo.data.model.MessageModel;
import com.bytedance.firstDemo.data.model.SystemMessageModel;
import com.bytedance.firstDemo.data.repository.FriendRepository;
import com.bytedance.firstDemo.data.repository.MessageRepository;
import com.bytedance.firstDemo.data.user.LoginStateManager;
import com.bytedance.firstDemo.feature.chat.ChatActivity;
import com.bytedance.firstDemo.feature.home.HomeActivity;
import com.bytedance.firstDemo.feature.search.SearchActivity;
import com.bytedance.firstDemo.utils.MessageCenter;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.CenterPopupView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息列表 Fragment。
 * 展示每个会话最新一条消息，并支持：
 * 1. 分页加载
 * 2. 下拉刷新
 * 3. 添加好友
 * 4. 模拟消息推送
 * 5. 修改备注
 * 6. 查看调试数据
 */
public class MessageFragment extends Fragment {

    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    private MessageViewModel vm;
    private boolean isPushRunning = false;

    private final List<MessageModel> messageList = new ArrayList<>();

    // 新增：状态视图引用
    private View emptyView;
    private View errorView;
    private View loadingView;
    private View skeletonView;

    private long stayStartTime = 0;



    // ---- Fragment 生命周期 ----

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        // ====== 页面入口埋点（默认 nav，可根据业务传参覆盖） ======
        Map<String, String> entryParams = new HashMap<>();
        entryParams.put("source", "nav");
        MetricsCenter.get().track("message_list_entry", entryParams);

        // ====== 页面停留监测：初始化时间 ======
        stayStartTime = 0;

        View view = inflater.inflate(R.layout.fragment_message, container, false);

        ImageView ivMenu = view.findViewById(R.id.ivMenu);
        ImageView ivSearch = view.findViewById(R.id.ivSearch);
        ImageView ivAdd = view.findViewById(R.id.ivAdd);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        vm = new ViewModelProvider(this).get(MessageViewModel.class);
        vm.init(requireContext());

        rvMessages = view.findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MessageAdapter(new ArrayList<>());
        rvMessages.setAdapter(adapter);

        emptyView = view.findViewById(R.id.layoutEmpty);
        errorView = view.findViewById(R.id.layoutError);
        loadingView = view.findViewById(R.id.layoutLoading);

        skeletonView = view.findViewById(R.id.layoutSkeleton);

        if (errorView != null) {
            View btnRetry = errorView.findViewById(R.id.btnRetry);
            if (btnRetry != null) btnRetry.setOnClickListener(v -> vm.simulateNetworkLoadSessions());
        }

        ivMenu.setOnClickListener(v -> {
            MetricsCenter.get().track("message_click_negative");
            if (requireActivity() instanceof HomeActivity) {
                ((HomeActivity) requireActivity()).openNegativeDrawer();
            }
        });

        ivSearch.setOnClickListener(v -> {
            MetricsCenter.get().track("message_click_search");
            Intent it = new Intent(getContext(), SearchActivity.class);
            startActivity(it);
        });

        initRefresh();

        // ====== 点击会话埋点（新增 unread_count_before_click） ======
        adapter.setOnItemClickListener(model -> {

            // 好友会话
            if (model.getType() == MessageModel.TYPE_FRIEND) {

                // —— 埋点：未读数 before click ——
                Map<String, String> unreadParams = new HashMap<>();
                unreadParams.put("sessionId", model.sessionId);
                unreadParams.put("unread", String.valueOf(model.unread));
                MetricsCenter.get().track("unread_count_before_click", unreadParams);

                // —— 埋点：点击会话 ——
                Map<String, String> params = new HashMap<>();
                params.put("sessionId", model.sessionId);
                params.put("friendName", model.sender);
                MetricsCenter.get().track("message_click", params);

                int friendId = Integer.parseInt(model.sessionId.replace("friend_", ""));
                vm.clearUnreadForFriend(friendId);
                vm.loadSessions();
                openChatPage(model);
                return;
            }

            // 系统消息
            if (model.getType() == MessageModel.TYPE_SYSTEM) {

                MetricsCenter.get().track("system_message_click");

                vm.clearSystemUnread();
                vm.loadSessions();

                new AlertDialog.Builder(requireContext())
                        .setTitle("系统消息详情（占位）")
                        .setMessage("这里将来是系统消息详情页。")
                        .setPositiveButton("知道了", null)
                        .show();
            }
        });

        adapter.setOnItemLongClickListener(model -> {
            if (model.getType() == MessageModel.TYPE_FRIEND) showSessionMenu(model);
        });

        // ========== 列表渲染完成（新增：系统消息曝光埋点） ==========
        vm.sessions.observe(getViewLifecycleOwner(), list -> {

            adapter.updateList(list);
            rvMessages.smoothScrollToPosition(0);

            // —— 页面展示埋点（你已有的） ——
            MetricsCenter.get().track("message_list_show");

            // —— 新增：系统消息曝光埋点 ——（首条消息为系统消息）
            if (list.size() > 0 && list.get(0).getType() == MessageModel.TYPE_SYSTEM) {
                MetricsCenter.get().track("system_message_exposure");
            }

            int totalUnread = 0;
            for (MessageModel m : list) totalUnread += m.unread;
            ((HomeActivity) requireActivity()).updateGlobalUnread(totalUnread);

            if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
        });

        vm.loadState.observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            switch (state) {
                case SKELETON:
                    showSkeleton();
                    break;
                case LOADING:
                    showLoading();
                    break;
                case EMPTY:
                    showEmpty();
                    break;
                case ERROR:
                    showError("加载失败，请重试");
                    break;
                case TIMEOUT:
                    showError("网络超时，请重试");
                    break;
                case SUCCESS:
                default:
                    showList();
                    break;
            }

            if (swipeRefresh.isRefreshing()
                    && state != MessageViewModel.LoadState.LOADING
                    && state != MessageViewModel.LoadState.SKELETON) {
                swipeRefresh.setRefreshing(false);
            }
        });

        ivAdd.setOnClickListener(v -> {
            MetricsCenter.get().track("message_click_plus");
            showPlusMenu(v);
        });

        vm.simulateNetworkLoadSessions();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        // 页面进入埋点 —— 新增
        MetricsCenter.get().track("message_list_enter");

        // 页面停留开始
        stayStartTime = System.currentTimeMillis();
        MetricsCenter.get().track("message_list_stay_start");
    }



    @Override
    public void onPause() {
        super.onPause();

        if (stayStartTime > 0) {
            long duration = System.currentTimeMillis() - stayStartTime;

            Map<String, String> params = new HashMap<>();
            params.put("duration_ms", String.valueOf(duration));

            // 结束停留（全部停留）
            MetricsCenter.get().track("message_list_stay_end", params);

            // 新增：有效停留（>1 秒）
            if (duration >= 1000) {
                Map<String, String> validParams = new HashMap<>();
                validParams.put("duration_ms", String.valueOf(duration));
                MetricsCenter.get().track("message_list_valid_stay", validParams);
            }
        }
    }



    // ===== 下拉刷新 =====

    /**
     * 下拉刷新初始化：改为模拟网络加载（弱网 / 超时 / 首刷失败）
     */
    private void initRefresh() {
        swipeRefresh.setColorSchemeColors(Color.WHITE, Color.RED, Color.YELLOW);

        swipeRefresh.setOnRefreshListener(() -> {

            // —— 埋点：message_list_refresh ——
            MetricsCenter.get().track("message_list_refresh");

            vm.simulateNetworkLoadSessions();
        });
    }


    // ===== 添加好友 =====

    /**
     * 展示“添加好友”对话框。
     * 用户输入昵称后写入 SQLite。
     */
    private void showAddFriendDialog() {
        new XPopup.Builder(requireContext())
                .hasShadowBg(true)
                .hasBlurBg(true)
                .asInputConfirm(
                        "添加好友",
                        null,
                        "请输入好友昵称",
                        text -> vm.addFriend(text)
                )
                .show();
    }




    // ===== 查看所有好友 =====

    /**
     * 展示 friend 表全部字段内容（调试用）。
     */
    private void showAllFriendInfo() {
        String owner = LoginStateManager.getCurrentAccount(requireContext());
        FriendRepository repo = new FriendRepository(requireContext());
        List<FriendModel> friends = repo.getAllFriends();

        new XPopup.Builder(requireContext())
                .hasShadowBg(true)
                .hasBlurBg(true)
                .asCustom(new FriendListDialog(requireContext(), owner, friends))
                .show();
    }


    private static class FriendListDialog extends CenterPopupView {

        private final String owner;
        private final List<FriendModel> data;

        public FriendListDialog(@NonNull Context ctx, String owner, List<FriendModel> data) {
            super(ctx);
            this.owner = owner;
            this.data = data;
        }

        @Override
        protected int getImplLayoutId() {
            return R.layout.dialog_all_friends;
        }

        @Override
        protected void onCreate() {
            super.onCreate();
            TextView tvTitle = findViewById(R.id.tvTitle);
            LinearLayout container = findViewById(R.id.container);

            tvTitle.setText("账号：" + owner + " 的所有好友");

            for (FriendModel f : data) {
                TextView tv = new TextView(getContext());
                tv.setText(
                        "ID：" + f.friendId + "\n" +
                                "昵称：" + f.nickname + "\n" +
                                "备注：" + f.remark + "\n" +
                                "最后消息：" + f.lastMessage + "\n" +
                                "时间：" + f.lastTime
                );
                tv.setTextColor(0xFF444444);
                tv.setPadding(0, 10, 0, 10);
                container.addView(tv);
            }
        }
    }


    // ===== 顶部右上角加号菜单 =====

    /**
     * 展示顶部右侧加号菜单（查看好友、添加好友、查看消息、推送模拟等）。
     */
    private void showPlusMenu(View anchor) {
        View popView = LayoutInflater.from(requireContext()).inflate(R.layout.menu_pop, null);

        PopupWindow popupWindow = new PopupWindow(
                popView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popView.getMeasuredWidth();
        int offsetX = anchor.getWidth() - popupWidth;
        int offsetY = (int) (8 * getResources().getDisplayMetrics().density);

        popupWindow.showAsDropDown(anchor, offsetX, offsetY);

        popView.findViewById(R.id.btnGroupChat).setOnClickListener(v -> {
            popupWindow.dismiss();
            showAllFriendInfo();
        });

        popView.findViewById(R.id.btnAddFriend).setOnClickListener(v -> {
            popupWindow.dismiss();
            showAddFriendDialog();
        });

        popView.findViewById(R.id.btnScan).setOnClickListener(v -> {
            popupWindow.dismiss();
            showAllMessages();
//            showClearMessagesDialog();
        });

        popView.findViewById(R.id.btnSpark).setOnClickListener(v -> {
            popupWindow.dismiss();
            showAddChatDialog();
        });
        popView.findViewById(R.id.btnAddSystemMessage).setOnClickListener(v -> {
            popupWindow.dismiss();
            showAddSystemMessageDialog();
        });

        popView.findViewById(R.id.btnShowSystemMessages).setOnClickListener(v -> {
            popupWindow.dismiss();
            showAllSystemMessages();
        });
        // ========== 清空所有未读 ==========
        popView.findViewById(R.id.btnClearUnread).setOnClickListener(v -> {
            popupWindow.dismiss();

            vm.clearAllUnread(); // ViewModel 执行清空逻辑

            Toast.makeText(requireContext(), "已清空全部未读消息", Toast.LENGTH_SHORT).show();
        });


        LinearLayout btnTogglePush = popView.findViewById(R.id.btnTogglePush);
        TextView tvToggle = popView.findViewById(R.id.tvTogglePush);
        tvToggle.setText(isPushRunning ? "关闭消息推送" : "开启消息推送");

        btnTogglePush.setOnClickListener(v -> {
            popupWindow.dismiss();

            FriendRepository repo = new FriendRepository(requireContext());

            if (!isPushRunning) {
                MessageCenter.getInstance().start(vm, repo);
                isPushRunning = true;
                Toast.makeText(requireContext(), "消息推送已开启", Toast.LENGTH_SHORT).show();
            } else {
                MessageCenter.getInstance().stop();
                isPushRunning = false;
                Toast.makeText(requireContext(), "消息推送已关闭", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== 添加对话消息 =====

    /**
     * 展示“添加消息”对话框，支持好友/群聊、发送/接收不同模式。
     */
    private void showAddChatDialog() {
        new XPopup.Builder(requireContext())
                .hasShadowBg(true)
                .hasBlurBg(true)
                .asCustom(new AddChatDialog(requireContext(), (session, content) ->
                        vm.addChatMessage(session, content)
                ))
                .show();
    }



    /**
     * 新增：添加一条系统消息（互动消息）
     */
    private void showAddSystemMessageDialog() {
        new XPopup.Builder(requireContext())
                .hasShadowBg(true)
                .hasBlurBg(true)
                .asCustom(new AddSystemMessageDialog(requireContext(), (sender, action, extra) -> {

                    if (extra == null) {
                        vm.addInteractionMessage(sender + action);
                    } else {
                        vm.addInteractionMessage(sender + action + "：" + extra);
                    }

                }))
                .show();
    }
    private static class AddSystemMessageDialog extends CenterPopupView {

        interface Callback {
            void onSubmit(String sender, String action, @Nullable String extraContent);
        }

        private final Callback callback;

        public AddSystemMessageDialog(@NonNull Context context, Callback callback) {
            super(context);
            this.callback = callback;
        }

        @Override
        protected int getImplLayoutId() {
            return R.layout.dialog_add_system_message;  // 接下来我会自动生成这个 XML
        }

        @Override
        protected void onCreate() {
            super.onCreate();

            EditText etSender = findViewById(R.id.etSender);

            // 五种动作按钮
            TextView btnLike = findViewById(R.id.btnLike);
            TextView btnFollow = findViewById(R.id.btnFollow);
            TextView btnAt = findViewById(R.id.btnAt);
            TextView btnReply = findViewById(R.id.btnReply);
            TextView btnComment = findViewById(R.id.btnComment);

            findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());

            // ========== 公共处理动作 ==========
            View.OnClickListener listener = v -> {
                String sender = etSender.getText().toString().trim();
                if (sender.isEmpty()) {
                    Toast.makeText(getContext(), "发送人不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ===== 先计算动作，不要让 lambda 捕获可变变量 =====
                final String action;
                final boolean needExtra;

                int id = v.getId();
                if (id == R.id.btnLike) {
                    action = "赞了你";
                    needExtra = false;
                } else if (id == R.id.btnFollow) {
                    action = "关注了你";
                    needExtra = false;
                } else if (id == R.id.btnAt) {
                    action = "@了你";
                    needExtra = false;
                } else if (id == R.id.btnReply) {
                    action = "回复了你";
                    needExtra = true;
                } else if (id == R.id.btnComment) {
                    action = "评论了你";
                    needExtra = true;
                } else {
                    return;
                }

                // ===== 不需要额外输入，直接返回 =====
                if (!needExtra) {
                    callback.onSubmit(sender, action, null);
                    dismiss();
                    return;
                }

                // ===== 需要内容输入（回复 / 评论）=====
                new XPopup.Builder(getContext())
                        .asInputConfirm(
                                action,
                                null,
                                "请输入" + action + "的内容",
                                extra -> callback.onSubmit(sender, action, extra)   // ✔ 现在是 final 变量，可以被 lambda 捕获
                        )
                        .show();

                dismiss();
            };


            btnLike.setOnClickListener(listener);
            btnFollow.setOnClickListener(listener);
            btnAt.setOnClickListener(listener);
            btnReply.setOnClickListener(listener);
            btnComment.setOnClickListener(listener);
        }
    }



    private static class AddChatDialog extends CenterPopupView {

        interface Callback {
            void onSubmit(String session, String content);
        }

        private final Callback callback;

        public AddChatDialog(@NonNull Context context, Callback callback) {
            super(context);
            this.callback = callback;
        }

        @Override
        protected int getImplLayoutId() {
            return R.layout.dialog_add_chat; // 你已经有这个 xml
        }

        @Override
        protected void onCreate() {
            super.onCreate();

            EditText etSession = findViewById(R.id.etSession);
            EditText etContent = findViewById(R.id.etContent);

            findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
            findViewById(R.id.btnConfirm).setOnClickListener(v -> {
                String s = etSession.getText().toString().trim();
                String c = etContent.getText().toString().trim();

                if (s.isEmpty() || c.isEmpty()) {
                    Toast.makeText(getContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                callback.onSubmit(s, c);
                dismiss();
            });
        }
    }


    // ===== 调试：查看 message 表 =====

    /**
     * 展示 message 表的所有消息记录（调试用）。
     */
    private void showAllMessages() {
        MessageRepository repo = new MessageRepository(requireContext());
        List<MessageModel> list = repo.getAllMessages();

        new XPopup.Builder(requireContext())
                .hasShadowBg(true)
                .hasBlurBg(true)
                .asCustom(new AllMessagesDialog(requireContext(), list))
                .show();
    }
    private static class AllMessagesDialog extends CenterPopupView {

        private final List<MessageModel> data;

        public AllMessagesDialog(@NonNull Context ctx, List<MessageModel> data) {
            super(ctx);
            this.data = data;
        }

        @Override
        protected int getImplLayoutId() {
            return R.layout.dialog_all_messages;
        }

        @Override
        protected void onCreate() {
            super.onCreate();

            LinearLayout container = findViewById(R.id.container);

            for (MessageModel m : data) {
                TextView tv = new TextView(getContext());
                tv.setText(
                        "会话：" + m.sessionId + "\n" +
                                "内容：" + m.content + "\n" +
                                "时间：" + m.getTime()
                );
                tv.setPadding(0, 10, 0, 10);
                tv.setTextColor(0xFF444444);
                container.addView(tv);
            }
        }
    }



    /**
     * 展示 system_message 表中的全部系统消息。
     */
    private void showAllSystemMessages() {
        List<SystemMessageModel> list = vm.getAllSystemMessages();

        new XPopup.Builder(requireContext())
                .hasShadowBg(true)
                .hasBlurBg(true)
                .asCustom(new SystemMessageListDialog(requireContext(), list))
                .show();
    }

    private static class SystemMessageListDialog extends CenterPopupView {
        private final List<SystemMessageModel> data;

        public SystemMessageListDialog(@NonNull Context ctx, List<SystemMessageModel> data) {
            super(ctx);
            this.data = data;
        }

        @Override
        protected int getImplLayoutId() {
            return R.layout.dialog_all_system_messages; // 你的 xml 已存在
        }

        @Override
        protected void onCreate() {
            super.onCreate();
            LinearLayout container = findViewById(R.id.container);

            for (SystemMessageModel m : data) {
                TextView tv = new TextView(getContext());
                tv.setText(
                        "标题：" + m.title + "\n" +
                                "内容：" + m.content + "\n" +
                                "时间：" + m.time
                );
                tv.setTextSize(15);
                tv.setTextColor(0xFF444444);
                tv.setPadding(0, 10, 0, 10);
                container.addView(tv);
            }
        }
    }




    // ===== 清空消息 & 清空好友 =====

    /**
     * 弹窗确认清空 message 表。
     */
    private void showClearMessagesDialog() {
        new AlertDialog.Builder(requireContext())


                .setTitle("清空消息")
                .setMessage("确定要删除 message 表中的所有消息吗？此操作不可恢复。")
                .setPositiveButton("确定", (d, w) -> {
                    vm.clearAllMessages();
                    Toast.makeText(requireContext(), "消息已清空", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 弹窗确认清空 friend 表。
     */
    private void showClearFriendsDialog() {
        new AlertDialog.Builder(requireContext())


                .setTitle("清空好友")
                .setMessage("确定要删除 friend 表中所有好友数据吗？该操作不可恢复。")
                .setPositiveButton("确定", (d, w) -> {
                    vm.clearAllFriends();
                    Toast.makeText(requireContext(), "好友已清空", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ===== 修改备注 =====

    /**
     * 弹窗修改好友备注。
     *
     * @param friendId    好友 ID
     * @param currentName 当前昵称或备注
     */
    private void showEditRemarkDialog(int friendId, String currentName) {
        EditText input = new EditText(requireContext());
        input.setText(currentName);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(requireContext())


                .setTitle("修改备注")
                .setView(input)
                .setPositiveButton("保存", (d, w) -> {
                    String remark = input.getText().toString().trim();
                    if (remark.isEmpty()) remark = "";
                    vm.updateRemark(friendId, remark);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 进入聊天页面。
     * 从被点击的 MessageModel 中提取：sessionId、friendId、昵称、头像，
     * 然后启动 ChatActivity。
     */
    private void openChatPage(MessageModel model) {

        // ------- 获取 sessionId -------
        String sessionId = model.sessionId;   // 例如 "friend_3"

        // ------- 解析 friendId -------
        int friendId = -1;
        try {
            friendId = Integer.parseInt(sessionId.replace("friend_", ""));
        } catch (Exception ignored) {
        }

        // ------- 获取好友昵称-------
        String nickname = model.sender;

        // ------- 获取好友头像-------
        String avatar = model.avatarPath;

        // ------- 启动聊天页 -------
        Intent it = new Intent(requireContext(), ChatActivity.class);
        it.putExtra("sessionId", sessionId);
        it.putExtra("friendId", friendId);
        it.putExtra("friendNickname", nickname);
        it.putExtra("friendAvatar", avatar);

        startActivity(it);
    }

    // 置顶 / 取消置顶
    // 置顶 / 取消置顶
    private void showSessionMenu(MessageModel model) {
        int friendId = Integer.parseInt(model.sessionId.replace("friend_", ""));
        FriendRepository repo = new FriendRepository(requireContext());
        FriendModel f = repo.getFriendById(friendId);

        if (f == null) return;

        String title = (f.remark != null && !f.remark.isEmpty()) ? f.remark : f.nickname;

        new XPopup.Builder(requireContext())
                .hasShadowBg(true)
                .hasBlurBg(true)
                .asCustom(new SessionMenuDialog(requireContext(), title, new SessionMenuDialog.Callback() {
                    @Override
                    public void onPin() {
                        if (f.isPinned == 1) repo.unpinFriend(friendId);
                        else repo.pinFriend(friendId);
                        vm.loadSessions();
                    }

                    @Override
                    public void onHide() {
                        repo.hideFriend(friendId);
                        vm.loadSessions();
                    }

                    @Override
                    public void onDelete() {
                        repo.deleteFriend(friendId);
                        vm.loadSessions();
                    }

                    @Override
                    public void onRemark() {
                        showEditRemarkDialog(friendId, title);
                    }
                }))
                .show();
    }

    private static class SessionMenuDialog extends CenterPopupView {

        interface Callback {
            void onPin();
            void onHide();
            void onDelete();
            void onRemark();
        }

        private final String title;
        private final Callback cb;

        public SessionMenuDialog(@NonNull Context ctx, String title, Callback cb) {
            super(ctx);
            this.title = title;
            this.cb = cb;
        }

        @Override
        protected int getImplLayoutId() {
            return R.layout.dialog_session_menu;
        }

        @Override
        protected void onCreate() {
            super.onCreate();

            ((TextView) findViewById(R.id.tvTitle)).setText(title);

            findViewById(R.id.btnPin).setOnClickListener(v -> { cb.onPin(); dismiss(); });
            findViewById(R.id.btnHide).setOnClickListener(v -> { cb.onHide(); dismiss(); });
            findViewById(R.id.btnDelete).setOnClickListener(v -> { cb.onDelete(); dismiss(); });
            findViewById(R.id.btnRemark).setOnClickListener(v -> { cb.onRemark(); dismiss(); });
        }
    }







    private void showSkeleton() {
        skeletonView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        rvMessages.setVisibility(View.GONE);
    }

    private void showLoading() {
        skeletonView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        rvMessages.setVisibility(View.GONE);
    }

    private void showList() {
        skeletonView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        rvMessages.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        skeletonView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        rvMessages.setVisibility(View.GONE);
    }

    private void showError(String text) {
        skeletonView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        rvMessages.setVisibility(View.GONE);

        TextView tv = errorView.findViewById(R.id.tvErrorMsg);
        if (tv != null) tv.setText(text);
    }

}

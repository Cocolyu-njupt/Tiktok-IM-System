package com.bytedance.firstDemo.feature.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.model.FriendModel;
import com.bytedance.firstDemo.data.model.MessageModel;
import com.bytedance.firstDemo.data.repository.FriendRepository;
import com.bytedance.firstDemo.data.repository.UserRepository;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private int friendId;
    private String friendNickname;
    private String friendAvatarPath;
    private String sessionId;

    private ChatAdapter adapter;
    private ChatViewModel vm;

    private androidx.recyclerview.widget.RecyclerView rvChat;
    private SwipeRefreshLayout swipeChat;

    private boolean isLoadingMore = false;


    // 记录分页前的顶部位置 & 偏移，用于保持锚点不动
    private int lastTopPosition = 0;
    private int lastTopOffset = 0;
    private int lastOldSize = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendId = getIntent().getIntExtra("friendId", -1);
        sessionId = getIntent().getStringExtra("sessionId");

        FriendRepository friendRepo = new FriendRepository(this);
        FriendModel f = friendRepo.getFriendById(friendId);

        if (f != null) {
            friendNickname = (f.remark != null && !f.remark.isEmpty()) ? f.remark : f.nickname;
            friendAvatarPath = f.avatar;
        } else {
            friendNickname = "好友";
            friendAvatarPath = null;
        }

        initTopBar();
        initRecyclerView();
        initSwipeRefresh();
        initInputBar();

        vm = new ViewModelProvider(this).get(ChatViewModel.class);
        vm.initChat(friendId, sessionId, friendAvatarPath);

        vm.chatList.observe(this, list -> {
            if (list == null) list = java.util.Collections.emptyList();

            if (!isLoadingMore) {
                // 正常刷新（首次进入 / 发送消息）
                adapter.updateList(list);
                scrollToBottom();
            } else {
                // 分页加载更多：需要保持锚点不动
                adapter.updateList(list);

                LinearLayoutManager lm = (LinearLayoutManager) rvChat.getLayoutManager();
                if (lm != null) {
                    int newSize = list.size();
                    int added = newSize - lastOldSize;   // 本次新加了多少个 item（旧消息 + 时间条）

                    if (added > 0) {
                        int targetPos = lastTopPosition + added;
                        lm.scrollToPositionWithOffset(targetPos, lastTopOffset);
                    }
                }
            }

            if (swipeChat.isRefreshing()) {
                swipeChat.setRefreshing(false);
            }
            isLoadingMore = false;
        });

    }

    // ------------------- 顶部栏 -------------------
    private void initTopBar() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // TODO: 你顶部昵称 TextView 的 id 自己改
         ((TextView) findViewById(R.id.tvNickname)).setText(friendNickname);
    }

    // ------------------- RecyclerView -------------------
    private void initRecyclerView() {
        swipeChat = findViewById(R.id.swipeChat);
        rvChat = findViewById(R.id.rvChat);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        rvChat.setLayoutManager(lm);

        String myAvatar = new UserRepository(this).getUserAvatar(this);

        adapter = new ChatAdapter(this, myAvatar, friendAvatarPath);
        rvChat.setAdapter(adapter);
    }

    // ------------------- 下拉加载更多 -------------------
    private void initSwipeRefresh() {

        swipeChat.setColorSchemeResources(
                android.R.color.white,
                android.R.color.holo_blue_bright
        );

        swipeChat.setOnRefreshListener(() -> {
            if (adapter.getItemCount() == 0) {
                swipeChat.setRefreshing(false);
                return;
            }

            // 记录当前顶部可见 item 的位置和偏移，用于后续还原
            LinearLayoutManager lm = (LinearLayoutManager) rvChat.getLayoutManager();
            if (lm != null) {
                lastTopPosition = lm.findFirstVisibleItemPosition();
                View topView = lm.findViewByPosition(lastTopPosition);
                lastTopOffset = (topView == null) ? 0 : topView.getTop();
                lastOldSize = adapter.getItemCount();
            }

            isLoadingMore = true;
            vm.loadMore();
        });
    }


    // ------------------- 输入栏（发送消息） -------------------
    private void initInputBar() {

        EditText etMessage = findViewById(R.id.etMessage);
        ImageView btnPlus = findViewById(R.id.btnPlus);

        // 软键盘右下角 “发送”
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage(etMessage);
                return true;
            }
            return false;
        });

        // PC 回车
        etMessage.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_DOWN) {
                sendMessage(etMessage);
                return true;
            }
            return false;
        });

        btnPlus.setOnClickListener(v -> sendMessage(etMessage));
    }

    // ------------------- 实际发送消息 -------------------
    private void sendMessage(EditText et) {
        String content = et.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        vm.sendMessage(content);
        et.setText("");

        scrollToBottom();
    }

    private void scrollToBottom() {
        rvChat.post(() ->
                rvChat.scrollToPosition(
                        adapter.getItemCount() - 1
                )
        );
    }
}

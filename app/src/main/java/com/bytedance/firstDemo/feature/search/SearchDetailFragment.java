package com.bytedance.firstDemo.feature.search;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.model.FriendModel;
import com.bytedance.firstDemo.data.repository.FriendRepository;
import com.bytedance.firstDemo.feature.chat.ChatActivity;
import com.bytedance.firstDemo.feature.message.MessageViewModel;
import com.bytedance.firstDemo.utils.AvatarUtils;

public class SearchDetailFragment extends Fragment {

    private static final String ARG_SESSION = "session";
    private static final String ARG_KEYWORD = "keyword";
    private static final String ARG_NAME = "name";
    private static final String ARG_AVATAR = "avatar";
    private static final String ARG_FRIEND_ID = "friendId";

    private MessageViewModel vm;

    private String sessionId;
    private String keyword;
    private String friendName;
    private String friendAvatar;
    private int friendId;

    private SearchMessageAdapter adapter;

    public static SearchDetailFragment newInstance(String sessionId, String keyword,
                                                   String name, String avatar, int friendId) {

        Bundle b = new Bundle();
        b.putString(ARG_SESSION, sessionId);
        b.putString(ARG_KEYWORD, keyword);
        b.putString(ARG_NAME, name);
        b.putString(ARG_AVATAR, avatar);
        b.putInt(ARG_FRIEND_ID, friendId);

        SearchDetailFragment f = new SearchDetailFragment();
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_detail, container, false);

        vm = new ViewModelProvider(requireActivity()).get(MessageViewModel.class);
        vm.init(requireContext());

        sessionId = getArguments().getString(ARG_SESSION);
        keyword = getArguments().getString(ARG_KEYWORD);
        friendName = getArguments().getString(ARG_NAME, "");
        friendAvatar = getArguments().getString(ARG_AVATAR, "");
        friendId = getArguments().getInt(ARG_FRIEND_ID, -1);

        /*
         * =============== 顶部搜索栏 ===============
         */
        View top = view.findViewById(R.id.search_top_root);
        ImageView ivBack = top.findViewById(R.id.ivBack);
        EditText etSearch = top.findViewById(R.id.etSearch);
        ImageView ivClear = top.findViewById(R.id.ivClear);
        TextView tvFriendName = top.findViewById(R.id.tvFriendName);

        ivBack.setVisibility(View.VISIBLE);
        tvFriendName.setVisibility(View.VISIBLE);

        // 初始显示 “好友名 + 空格 + keyword”
        String combined = friendName + " " + keyword;

        SpannableString span = new SpannableString(combined);
        span.setSpan(
                new ForegroundColorSpan(Color.YELLOW),
                0,
                friendName.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        etSearch.setText(span);
        etSearch.setSelection(combined.length());
        ivClear.setVisibility(combined.trim().isEmpty() ? View.GONE : View.VISIBLE);

        ivClear.setOnClickListener(v -> etSearch.setText(""));
        ivBack.setOnClickListener(v -> requireActivity().onBackPressed());


        /*
         * =============== 初始化 RecyclerView + Adapter（必须在 TextWatcher 前） ===============
         */
        RecyclerView rv = view.findViewById(R.id.rvDetail);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // ★ 创建全局 adapter（避免被局部变量覆盖）
        adapter = new SearchMessageAdapter(friendId, keyword, this::onMessageClick);
        rv.setAdapter(adapter);

        // 监听搜索结果
        vm.sessionSearchResults.observe(getViewLifecycleOwner(), adapter::updateList);

        // 显示一级页面的 keyword 结果
        vm.searchMessagesInSession(sessionId, keyword);


        /*
         * =============== 搜索输入监听（安全、不会 NPE） ===============
         */
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable s) {

                String fullText = s.toString();

                // 保护好友名不被删掉
                if (!fullText.startsWith(friendName)) {
                    etSearch.setText(friendName + " ");
                    etSearch.setSelection(etSearch.getText().length());
                    return;
                }

                String realKeyword = "";
                if (fullText.length() > friendName.length()) {
                    realKeyword = fullText.substring(friendName.length()).trim();
                }

                ivClear.setVisibility(fullText.trim().isEmpty() ? View.GONE : View.VISIBLE);

                // ★ adapter 已经初始化且非 null，安全！
                adapter.setKeyword(realKeyword);

                if (realKeyword.isEmpty()) {
                    vm.clearSessionSearchResult();
                } else {
                    vm.searchMessagesInSession(sessionId, realKeyword);
                }
            }
        });


        /*
         * =============== 好友卡片 ===============
         */
        ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        TextView tvTitle = view.findViewById(R.id.tvTitle);

        tvTitle.setText(friendName);
        AvatarUtils.loadAvatar(requireContext(), friendAvatar, ivAvatar);

        View layoutFriendCard = view.findViewById(R.id.layoutFriendCard);
        layoutFriendCard.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), ChatActivity.class);
            it.putExtra("friendId", friendId);
            it.putExtra("sessionId", sessionId);
            it.putExtra("friendNickname", friendName);
            it.putExtra("friendAvatar", friendAvatar);
            startActivity(it);
        });

        return view;
    }







    private void onMessageClick(int messageId) {
        Intent it = new Intent(getContext(), ChatActivity.class);
        it.putExtra("friendId", friendId);
        it.putExtra("sessionId", sessionId);          // 新增
        it.putExtra("friendNickname", friendName);    // 新增
        it.putExtra("friendAvatar", friendAvatar);    // 新增
        it.putExtra("jumpToMessageId", messageId);    // 原有逻辑
        startActivity(it);
    }

}

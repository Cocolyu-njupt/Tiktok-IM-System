package com.bytedance.firstDemo.feature.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.bytedance.firstDemo.data.model.SearchConversationResult;
import com.bytedance.firstDemo.feature.message.MessageViewModel;
import com.bytedance.firstDemo.feature.search.SearchActivity;
import com.bytedance.firstDemo.feature.search.SearchConversationAdapter;

import java.util.ArrayList;

public class SearchAllResultFragment extends Fragment {

    private MessageViewModel vm;
    private SearchConversationAdapter adapter;

    private EditText etSearch;
    private ImageView ivClear;
    private ImageView ivBack;
    private TextView tvFriendName;

    private String currentKeyword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_all, container, false);

        vm = new ViewModelProvider(requireActivity()).get(MessageViewModel.class);
        vm.init(requireContext());

        // ========== 搜索栏 UI 绑定 ==========
        View top = view.findViewById(R.id.search_top_root);

        ivBack = top.findViewById(R.id.ivBack);
        tvFriendName = top.findViewById(R.id.tvFriendName);
        etSearch = top.findViewById(R.id.etSearch);
        ivClear = top.findViewById(R.id.ivClear);

        // 一级页：隐藏好友名
        tvFriendName.setVisibility(View.GONE);

        // 一级页：返回按钮可见并绑定行为（关闭整个 SearchActivity）
        ivBack.setVisibility(View.VISIBLE);
        ivBack.setOnClickListener(v -> requireActivity().finish());

        // ========== RecyclerView ==========
        RecyclerView rv = view.findViewById(R.id.rvSearchConv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchConversationAdapter(new ArrayList<>(), this::onItemClicked);
        rv.setAdapter(adapter);


        // ========== 输入监听 ==========
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentKeyword = s.toString().trim();
                ivClear.setVisibility(currentKeyword.isEmpty() ? View.GONE : View.VISIBLE);

                if (!currentKeyword.isEmpty()) {
                    vm.searchAllSessions(currentKeyword);
                } else {
                    adapter.updateList(new ArrayList<>()); // 清空结果
                }
            }
        });

        ivClear.setOnClickListener(v -> etSearch.setText(""));

        // ========== 观察数据 ==========
        vm.searchResults.observe(getViewLifecycleOwner(), list -> {
            if (list == null) return;
            adapter.updateList(list);
        });

        return view;
    }


    // ========== 点击进入二级页 ==========
    private void onItemClicked(SearchConversationResult item) {

        // 从 sessionId 解析 friendId
        int friendId = -1;
        try {
            friendId = Integer.parseInt(item.sessionId.replace("friend_", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 跳转二级搜索页
        ((SearchActivity) requireActivity()).openSearchDetail(
                item.sessionId,
                currentKeyword,
                item.name,
                item.avatar,
                friendId
        );
    }
}

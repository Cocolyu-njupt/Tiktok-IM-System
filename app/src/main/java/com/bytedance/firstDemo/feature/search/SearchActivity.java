package com.bytedance.firstDemo.feature.search;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.firstDemo.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 默认进入一级搜索页
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.searchContainer, new SearchAllResultFragment())
                .commit();
    }

    /**
     * 一级搜索页点击某个好友结果 → 进入二级页
     */
    public void openSearchDetail(
            String sessionId,
            String keyword,
            String friendName,
            String friendAvatar,
            int friendId
    ) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.searchContainer,
                        SearchDetailFragment.newInstance(
                                sessionId,
                                keyword,
                                friendName,
                                friendAvatar,
                                friendId
                        )
                )
                .addToBackStack(null)
                .commit();
    }
}

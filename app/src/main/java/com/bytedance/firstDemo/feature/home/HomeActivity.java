// 文件位置：com.bytedance.firstDemo.feature.home.HomeActivity
package com.bytedance.firstDemo.feature.home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.user.LoginStateManager;
import com.bytedance.firstDemo.data.repository.UserRepository;
import com.bytedance.firstDemo.core.AppContextHolder;
import com.bytedance.firstDemo.feature.friend.FriendFragment;
import com.bytedance.firstDemo.feature.mine.MineFragment;
import com.bytedance.firstDemo.feature.message.MessageFragment;
import com.bytedance.firstDemo.feature.login.LoginActivity;
import com.bytedance.firstDemo.feature.upload.UploadFragment;

public class HomeActivity extends AppCompatActivity {

    private final Fragment home = new HomeFragment();
    private final Fragment friend = new FriendFragment();
    private final Fragment message = new MessageFragment();
    private final Fragment mine = new MineFragment();
    private TextView navMessageBadge;
    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;


    private ImageView ivUserAvatar;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        AppContextHolder.init(this.getApplication());

        drawerLayout = findViewById(R.id.drawerLayout);
        // ===== 预测返回手势兼容：优先关闭负一屏 Drawer =====
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null
                        && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // 抽屉没开 -> 交回系统默认返回行为
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });


        // 挂载 NegativeFragment（只初始化一次）
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.negative_container,
                            new com.bytedance.firstDemo.feature.negative.NegativeFragment()
                    )

                    .commit();
        }


        initCustomNav();

        boolean toMine = getIntent().getBooleanExtra("navigateToMine", false);
        if (toMine) {
            TextView tvMine = findViewById(R.id.nav_mine);
            tvMine.performClick();
        } else {
            TextView tvHome = findViewById(R.id.nav_home);
            tvHome.performClick();
        }
    }


    private void initCustomNav() {
        TextView tvHome = findViewById(R.id.nav_home);
        TextView tvFriend = findViewById(R.id.nav_friend);
        TextView tvUpload = findViewById(R.id.nav_upload);
        TextView tvMessage = findViewById(R.id.nav_message);
        TextView tvMine = findViewById(R.id.nav_mine);

        navMessageBadge = findViewById(R.id.nav_message_badge);


        View.OnClickListener listener = v -> {
            resetNavText();
            int id = v.getId();
            if (id == R.id.nav_home) {
                showFragment(home);
                highlight(tvHome);
            } else if (id == R.id.nav_friend) {
                showFragment(friend);
                highlight(tvFriend);
            } else if (id == R.id.nav_message) {
                showFragment(message);
                highlight(tvMessage);
            } else if (id == R.id.nav_mine) {
                if (LoginStateManager.isLoggedIn(HomeActivity.this)) {
                    showFragment(mine);
                    highlight(tvMine);
                } else {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        };

        tvHome.setOnClickListener(listener);
        tvFriend.setOnClickListener(listener);
        tvMessage.setOnClickListener(listener);
        tvMine.setOnClickListener(listener);

        // 加号点击：跳转 UploadActivity
        tvUpload.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UploadFragment.class);
            startActivity(intent);
        });

        // 默认首页选中
        tvHome.performClick();
    }

    private void highlight(TextView tv) {
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(null, Typeface.BOLD);
    }

    private void resetNavText() {
        int[] ids = {R.id.nav_home, R.id.nav_friend, R.id.nav_message, R.id.nav_mine};
        for (int id : ids) {
            TextView tv = findViewById(id);
            tv.setTextColor(Color.parseColor("#888888"));
            tv.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    public void updateGlobalUnread(int totalUnread) {
        if (totalUnread > 0) {
            navMessageBadge.setVisibility(View.VISIBLE);
            if (totalUnread > 99) {
                navMessageBadge.setText("99+");
            } else {
                navMessageBadge.setText(String.valueOf(totalUnread));
            }
        } else {
            navMessageBadge.setVisibility(View.GONE);
        }

    }

    public void openNegativeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
        }
    }

    public void closeNegativeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
        }
    }




}

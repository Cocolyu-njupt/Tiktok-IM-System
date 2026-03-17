// 文件路径：com/bytedance/firstDemo/ui/splash/SplashActivity.java
package com.bytedance.firstDemo.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.data.user.LoginStateManager;
import com.bytedance.firstDemo.feature.home.HomeActivity;
import com.bytedance.firstDemo.feature.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private SplashViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        viewModel.prepare();

        viewModel.getReadyLiveData().observe(this, isReady -> {
            if (Boolean.TRUE.equals(isReady)) {
                if (LoginStateManager.isLoggedIn(this)) {
                    startActivity(new Intent(this, HomeActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                finish();
            }
        });
    }
}

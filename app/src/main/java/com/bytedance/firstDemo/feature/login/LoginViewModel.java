// LoginViewModel.java
package com.bytedance.firstDemo.feature.login;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bytedance.firstDemo.data.user.LoginStateManager;
import com.bytedance.firstDemo.data.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String account, String password) {
        if (!isValidAccount(account)) {
            loginResult.setValue(new LoginResult(false, "请输入有效的手机号或邮箱"));
            return;
        }
        if (password.length() < 6) {
            loginResult.setValue(new LoginResult(false, "密码不能少于6位"));
            return;
        }

        if (!repository.userExists(account)) {
            loginResult.setValue(new LoginResult(false, "账号不存在，前往注册"));
        } else if (!repository.validateLogin(account, password)) {
            loginResult.setValue(new LoginResult(false, "密码错误"));
        } else {
            String nickname = repository.getNicknameByAccount(account);
            Context context = getApplication().getApplicationContext();
            LoginStateManager.setLoggedIn(context, true);
            SharedPreferences.Editor editor = context.getSharedPreferences("login_state", Context.MODE_PRIVATE).edit();
            editor.putString("nickname", nickname);
            editor.putString("account", account);

            editor.apply();

            loginResult.setValue(new LoginResult(true, nickname));
        }
    }

    private boolean isValidAccount(String input) {
        return input.matches("^1[3-9]\\d{9}$") || input.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$");
    }

    public static class LoginResult {
        public final boolean success;
        public final String message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
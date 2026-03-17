package com.bytedance.firstDemo.feature.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.feature.home.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private EditText editTextPhone, editTextPassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);

        btnLogin.setOnClickListener(v -> {
            String account = editTextPhone.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            viewModel.login(account, password);
        });

        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("account", editTextPhone.getText().toString().trim());
            startActivity(intent);
        });

        ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            editTextPassword.setInputType(
                    isPasswordVisible
                            ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            ivTogglePassword.setImageResource(
                    isPasswordVisible ? R.drawable.ic_eye_open : R.drawable.ic_eye_closed
            );
            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        viewModel.getLoginResult().observe(this, result -> {
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            if (result.success) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });
        TextView btnForget=findViewById(R.id.forgetPWD);
        btnForget.setOnClickListener(v ->
                Toast.makeText(this, "点击忘记密码", Toast.LENGTH_SHORT).show()
        );
        LinearLayout btnWeChat = findViewById(R.id.btnWeChat);
        LinearLayout btnApple = findViewById(R.id.btnApple);

        btnWeChat.setOnClickListener(v ->
                Toast.makeText(this, "点击微信登录", Toast.LENGTH_SHORT).show()
        );

        btnApple.setOnClickListener(v ->
                Toast.makeText(this, "点击苹果登录", Toast.LENGTH_SHORT).show()
        );

    }
}

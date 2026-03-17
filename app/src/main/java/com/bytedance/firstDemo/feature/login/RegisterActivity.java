package com.bytedance.firstDemo.feature.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.feature.home.HomeActivity;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        EditText etAccount = findViewById(R.id.editTextAccount);
        EditText etPassword = findViewById(R.id.editTextPassword);
        EditText etName = findViewById(R.id.editTextName);
        Button btnRegister = findViewById(R.id.btnRegister);

        String prefilledAccount = getIntent().getStringExtra("account");
        if (prefilledAccount != null) etAccount.setText(prefilledAccount);

        btnRegister.setOnClickListener(v -> {
            viewModel.register(
                    etAccount.getText().toString().trim(),
                    etPassword.getText().toString().trim(),
                    etName.getText().toString().trim()
            );
        });

        viewModel.getRegisterResult().observe(this, result -> {
            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show();
            if (result.success) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });
    }
}

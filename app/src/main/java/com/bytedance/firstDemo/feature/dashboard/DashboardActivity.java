package com.bytedance.firstDemo.feature.dashboard;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.core.metrics.MetricsRepository;
import com.bytedance.firstDemo.feature.dashboard.DashboardViewModel;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel viewModel;
    private DashboardModuleAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 解决状态栏白底白字问题 & 让标题栏顶到最上方
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

            View decor = window.getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // 深色图标
        }

        setContentView(R.layout.activity_dashboard);
        initViewModel();
        initRecyclerView();
        initBackButton();
        observeData();

    }

    /* ============================================================
     * 初始化 ViewModel（依赖 MetricsRepository）
     * ============================================================ */
    private void initViewModel() {
        MetricsRepository repo = new MetricsRepository(this);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @SuppressWarnings("unchecked")
                    @NonNull
                    @Override
                    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) new DashboardViewModel(repo);
                    }
                }).get(DashboardViewModel.class);
    }

    /* ============================================================
     * 初始化 RecyclerView（卡片列表）
     * ============================================================ */
    private void initRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvDashboardModules);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 初次为空数据，等 LiveData 推送再更新
        adapter = new DashboardModuleAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
    }

    /* ============================================================
     * 监听 ViewModel 数据变化
     * ============================================================ */
    private void observeData() {
        viewModel.modules.observe(this, modules -> {
            if (modules != null) {
                adapter.updateData(modules);
            }
        });
    }

    /* ============================================================
     * 返回按钮处理
     * ============================================================ */
    private void initBackButton() {
        ImageView back = findViewById(R.id.ivBack);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }
    }
}

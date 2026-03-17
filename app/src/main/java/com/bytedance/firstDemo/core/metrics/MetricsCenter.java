package com.bytedance.firstDemo.core.metrics;

import android.content.Context;

import com.bytedance.firstDemo.data.user.LoginStateManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局埋点中心（增长策略统一中台）
 * --------------------------------------------
 * 作用：
 * - 接受业务方的 track() 调用（轻量）
 * - 自动补充：时间戳、用户数据、页面信息
 * - 自动序列化为 MetricEvent
 * - 写入 SQLite（通过 MetricsRepository）
 * - 未来可扩展上传/分析
 */
public class MetricsCenter {

    private static MetricsCenter instance;

    private final MetricsRepository repository;
    private Context appContext;

    private MetricsCenter(Context ctx) {
        this.appContext = ctx.getApplicationContext();
        this.repository = new MetricsRepository(appContext);
    }

    public static synchronized MetricsCenter init(Context ctx) {
        if (instance == null) {
            instance = new MetricsCenter(ctx);
        }
        return instance;
    }

    public static MetricsCenter get() {
        if (instance == null) {
            throw new IllegalStateException("MetricsCenter is not initialized. Call init(context) first.");
        }
        return instance;
    }

    /**
     * ---- 业务调用入口（只写一行） ----
     * MetricsCenter.get().track("event_name", map("key","value"));
     */
    public void track(String eventName) {
        track(eventName, null);
    }

    public void track(String eventName, Map<String, String> params) {
        try {
            MetricEvent event = new MetricEvent();
            event.eventName = eventName;
            event.timestamp = System.currentTimeMillis();

            // 当前登录用户（从你的 LoginStateManager 读取）
            String userId = LoginStateManager.getCurrentAccount(appContext);
            if (userId == null) userId = "unknown";
            event.userId = userId;

            // 自动记录页面（可选）
            // 如果未来你想自动捕获页面名，可以由调用方传入
            event.page = getCurrentPageName();

            // 序列化参数
            if (params == null) params = new HashMap<>();
            event.params = new JSONObject(params).toString();

            // Insert into SQLite
            repository.insertEvent(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动获取页面名（轻量，开发者可选择是否在业务传入 page 字段）
     */
    private String getCurrentPageName() {
        // 简化：此处可扩展为 ActivityLifecycleCallbacks
        // 暂时返回 "unknown" 或未来在 track() 中允许业务传 page 名称
        return "unknown";
    }
}


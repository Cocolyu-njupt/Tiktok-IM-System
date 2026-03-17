package com.bytedance.firstDemo.feature.dashboard;

/**
 * DashboardMetric
 *
 * 表示一条指标（标题 + 值）
 * 用于 Dashboard 的 Page1（原始数据）和 Page2（二级指标）
 */
public class DashboardMetric {

    public String title;   // 指标标题，如 "页面曝光次数"
    public String value;   // 指标值，如 "32 次"

    public DashboardMetric(String title, String value) {
        this.title = title;
        this.value = value;
    }
}

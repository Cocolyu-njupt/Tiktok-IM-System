package com.bytedance.firstDemo.feature.dashboard;

import com.bytedance.firstDemo.feature.dashboard.DashboardViewModel;

import java.util.List;

/**
 * DashboardModule
 *
 * 一个数据模块（消息 / 天气 / 聊天 / 搜索）
 * 对应 Dashboard 上的一张卡片
 *
 * 由 ViewModel 构建，Adapter 只做 UI 展示
 */
public class DashboardModule {

    public String moduleName;                         // 卡片标题，例如 "消息模块（Message）"

    public List<DashboardMetric> rawMetrics;          // Page1：原始指标
    public List<DashboardMetric> derivedMetrics;      // Page2：二级指标
    public DashboardViewModel.ChartData chartData;    // Page3：图表指标

    public DashboardModule(String moduleName) {
        this.moduleName = moduleName;
    }
}

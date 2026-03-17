package com.bytedance.firstDemo.core.metrics;

public class MetricEvent {
    public int id;                // 自增主键（SQLite 自动生成）
    public String eventName;
    public String page;
    public String userId;
    public long timestamp;        // 事件发生时间
    public String params;         // JSON 字符串（所有扩展参数）
}

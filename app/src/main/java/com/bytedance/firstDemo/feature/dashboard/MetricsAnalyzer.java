package com.bytedance.firstDemo.feature.dashboard;

import com.bytedance.firstDemo.core.metrics.MetricEvent;

import org.json.JSONObject;

import java.util.List;

/**
 * MetricsAnalyzer —— 增长分析引擎（可扩展）
 * 用于计算复合指标、召回率、停留时长、CTR 等
 */
public class MetricsAnalyzer {

    /** 计算平均停留时间（示例） */
    public static long calcAvgStayDuration(List<MetricEvent> events) {

        long total = 0;
        int count = 0;

        for (MetricEvent e : events) {
            if ("message_list_stay_end".equals(e.eventName)) {

                try {
                    JSONObject obj = new JSONObject(e.params);
                    long dur = obj.optLong("duration_ms", 0);
                    total += dur;
                    count++;
                } catch (Exception ignored) {}
            }
        }

        return count == 0 ? 0 : total / count;
    }

    /** 计算系统消息召回率（示例） */
    public static double calcSystemRecallRate(List<MetricEvent> events) {

        double exposure = 0;
        double click = 0;

        for (MetricEvent e : events) {
            if ("system_message_exposure".equals(e.eventName)) exposure++;
            if ("system_message_click".equals(e.eventName)) click++;
        }

        return exposure == 0 ? 0 : (click / exposure);
    }
}

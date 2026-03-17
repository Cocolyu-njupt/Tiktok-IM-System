package com.bytedance.firstDemo.feature.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bytedance.firstDemo.core.metrics.MetricEvent;
import com.bytedance.firstDemo.core.metrics.MetricsRepository;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardViewModel extends ViewModel {

    private final MetricsRepository repo;

    private final MutableLiveData<List<DashboardModule>> _modules = new MutableLiveData<>();
    public final LiveData<List<DashboardModule>> modules = _modules;

    public DashboardViewModel(MetricsRepository repo) {
        this.repo = repo;
        loadDashboardData();
    }

    /* ============================================================
     * 加载所有模块数据
     * ============================================================ */
    private void loadDashboardData() {
        List<MetricEvent> allEvents = repo.getAllEvents();

        List<DashboardModule> list = new ArrayList<>();
        list.add(buildMessageModule(allEvents));
        list.add(buildPlaceholderModule("天气模块（Weather）"));
        list.add(buildPlaceholderModule("聊天模块（Chat）"));
        list.add(buildPlaceholderModule("搜索模块（Search）"));

        _modules.setValue(list);
    }

    /* ============================================================
     * 占位模块（天气、聊天、搜索）
     * ============================================================ */
    private DashboardModule buildPlaceholderModule(String title) {
        DashboardModule m = new DashboardModule(title);
        m.rawMetrics = new ArrayList<>();
        m.derivedMetrics = new ArrayList<>();
        m.chartData = new ChartData();
        return m;
    }


    /* ============================================================
     * 消息模块：核心模块
     * ============================================================ */
    private DashboardModule buildMessageModule(List<MetricEvent> all) {

        DashboardModule module = new DashboardModule("消息模块（Message）");

        int exposure = 0;
        int clickFriend = 0;
        int clickSystem = 0;
        int plusClick = 0;
        int searchClick = 0;
        int negativeClick = 0;

        int validStay = 0;
        int invalidStay = 0;

        long totalDuration = 0;
        long validDuration = 0;

        long lastEnterTime = 0;
        long enterOps = 0;

        for (MetricEvent e : all) {
            switch (e.eventName) {

                case "message_list_enter":
                    exposure++;
                    enterOps++;
                    lastEnterTime = Math.max(lastEnterTime, e.timestamp);
                    break;

                case "message_click":
                    clickFriend++;
                    break;

                case "system_message_click":
                    clickSystem++;
                    break;

                case "message_click_plus":
                    plusClick++;
                    break;

                case "message_click_search":
                    searchClick++;
                    break;

                case "message_click_negative":
                    negativeClick++;
                    break;

                case "message_list_valid_stay":
                    validStay++;
                    try {
                        JSONObject obj = new JSONObject(e.params);
                        validDuration += obj.optLong("duration_ms", 0);
                    } catch (Exception ignore) {}
                    break;

                case "message_list_stay_end":
                    invalidStay++;
                    try {
                        JSONObject obj = new JSONObject(e.params);
                        totalDuration += obj.optLong("duration_ms", 0);
                    } catch (Exception ignore) {}
                    break;
            }
        }

        invalidStay = Math.max(0, invalidStay - validStay);

        long avgStay = exposure > 0 ? totalDuration / exposure : 0;
        long avgValidStay = validStay > 0 ? validDuration / validStay : 0;

        float ctr = exposure == 0 ? 0 : (clickFriend + clickSystem) * 1f / exposure * 100f;
        float recall = exposure == 0 ? 0 : clickSystem * 1f / exposure * 100f;

        List<DashboardMetric> raw = new ArrayList<>();
        raw.add(new DashboardMetric("页面曝光次数", exposure + " 次"));
        raw.add(new DashboardMetric("最后进入时间", timeFormat(lastEnterTime)));
        raw.add(new DashboardMetric("会话点击次数", clickFriend + " 次"));
        raw.add(new DashboardMetric("系统消息点击次数", clickSystem + " 次"));
        raw.add(new DashboardMetric("有效停留次数", validStay + " 次"));
        raw.add(new DashboardMetric("无效停留次数", invalidStay + " 次"));
        raw.add(new DashboardMetric("平均停留时长", (avgStay / 1000) + " 秒"));
        raw.add(new DashboardMetric("平均有效停留时长", (avgValidStay / 1000) + " 秒"));

        module.rawMetrics = raw;

        /* ============ 二级指标 ============ */
        List<DashboardMetric> derived = new ArrayList<>();
        derived.add(new DashboardMetric("点击率 CTR", String.format("%.2f%%", ctr)));
        derived.add(new DashboardMetric("有效停留占比", formatPercent(validStay, validStay + invalidStay)));
        derived.add(new DashboardMetric("平均停留时长", (avgStay / 1000) + " 秒"));
        derived.add(new DashboardMetric("平均有效停留时长", (avgValidStay / 1000) + " 秒"));
        derived.add(new DashboardMetric("系统消息召回率", String.format("%.2f%%", recall)));


        module.derivedMetrics = derived;

        module.chartData = buildMessageChartData(all);

        return module;
    }


    /* ============================================================
     * 图表数据计算（第三页）
     * ============================================================ */
    private ChartData buildMessageChartData(List<MetricEvent> all) {

        ChartData data = new ChartData();

        long now = System.currentTimeMillis();
        long oneDay = 24 * 3600 * 1000L;

        int[] validSum = new int[7];
        int[] validCnt = new int[7];

        for (int i = 0; i < 7; i++) {
            data.enterCountLast7Days.add(0);
            data.avgValidStayLast7Days.add(0f);
        }

        for (MetricEvent e : all) {

            long diff = now - e.timestamp;
            int dayIndex = (int) (diff / oneDay);

            switch (e.eventName) {
                case "message_click":
                    data.clickFriend++;
                    break;
                case "system_message_click":
                    data.clickSystem++;
                    break;
                case "message_click_plus":
                    data.clickPlus++;
                    break;
                case "message_click_search":
                    data.clickSearch++;
                    break;
                case "message_click_negative":
                    data.clickNegative++;
                    break;
            }

            if ("message_list_valid_stay".equals(e.eventName)) {
                data.validStayCount++;
                try {
                    JSONObject obj = new JSONObject(e.params);
                    long dur = obj.optLong("duration_ms", 0);
                    if (dayIndex >= 0 && dayIndex < 7) {
                        validSum[dayIndex] += dur;
                        validCnt[dayIndex]++;
                    }
                } catch (Exception ignore) {}
            }

            if ("message_list_stay_end".equals(e.eventName)) {
                data.invalidStayCount++;
            }

            if ("message_list_enter".equals(e.eventName)) {

                if (dayIndex >= 0 && dayIndex < 7) {
                    data.enterCountLast7Days.set(dayIndex,
                            data.enterCountLast7Days.get(dayIndex) + 1);
                }

                if (dayIndex == 1) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(e.timestamp);
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    data.enterCountByHour.set(hour, data.enterCountByHour.get(hour) + 1);
                }
            }
        }

        data.invalidStayCount = Math.max(0, data.invalidStayCount - data.validStayCount);

        for (int i = 0; i < 7; i++) {
            if (validCnt[i] > 0) {
                data.avgValidStayLast7Days.set(i, (validSum[i] / 1000f) / validCnt[i]);
            }
        }

        return data;
    }


    /* ---------------- 辅助方法 ---------------- */

    private String timeFormat(long t) {
        if (t <= 0) return "--";
        return new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(t));
    }

    private String formatPercent(int x, int total) {
        if (total == 0) return "0%";
        return String.format("%.2f%%", x * 100f / total);
    }

    private String scoreEnterWill(long cnt) {
        if (cnt == 0) return "0 分";
        if (cnt <= 3) return "60 分";
        if (cnt <= 10) return "80 分";
        return "100 分";
    }

    private String scoreStayWill(long sec) {
        if (sec <= 2) return "30 分";
        if (sec <= 10) return "60 分";
        if (sec <= 30) return "80 分";
        return "100 分";
    }

    private String scoreInteract(int cnt) {
        if (cnt == 0) return "0 分";
        if (cnt <= 2) return "40 分";
        if (cnt <= 5) return "60 分";
        return "80 分";
    }


    /* ============================================================
     * ChartData（最终版本）
     * ============================================================ */
    public static class ChartData {

        public int clickFriend;
        public int clickSystem;
        public int clickPlus;
        public int clickSearch;
        public int clickNegative;

        public int validStayCount;
        public int invalidStayCount;

        public List<Integer> enterCountLast7Days;
        public List<Float> avgValidStayLast7Days;
        public List<Integer> enterCountByHour;

        public ChartData() {
            enterCountLast7Days = new ArrayList<>();
            avgValidStayLast7Days = new ArrayList<>();
            enterCountByHour = new ArrayList<>(Collections.nCopies(24, 0));
        }
    }
}

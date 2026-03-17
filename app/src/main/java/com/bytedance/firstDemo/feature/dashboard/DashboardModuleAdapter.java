package com.bytedance.firstDemo.feature.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.feature.dashboard.DashboardViewModel.ChartData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardModuleAdapter（完整最终版）
 */
public class DashboardModuleAdapter extends RecyclerView.Adapter<DashboardModuleAdapter.ModuleVH> {

    private List<DashboardModule> modules;

    public DashboardModuleAdapter(List<DashboardModule> modules) {
        this.modules = modules;
    }

    public void updateData(List<DashboardModule> newData) {
        this.modules = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ModuleVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ModuleVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_module, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleVH holder, int position) {
        DashboardModule module = modules.get(position);
        holder.tvTitle.setText(module.moduleName);

        // 创建 ViewPager2 Adapter
        ModulePageAdapter pageAdapter =
                new ModulePageAdapter(module, holder.itemView.getContext());

        holder.viewPager.setAdapter(pageAdapter);

        // 左右箭头翻页
        holder.ivLeft.setOnClickListener(v -> {
            int cur = holder.viewPager.getCurrentItem();
            if (cur > 0) holder.viewPager.setCurrentItem(cur - 1, true);
        });

        holder.ivRight.setOnClickListener(v -> {
            int cur = holder.viewPager.getCurrentItem();
            if (cur < 2) holder.viewPager.setCurrentItem(cur + 1, true);
        });
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    /* =====================================================================
     * Module ViewHolder
     * ===================================================================== */
    static class ModuleVH extends RecyclerView.ViewHolder {

        TextView tvTitle;
        ViewPager2 viewPager;
        ImageView ivLeft, ivRight;

        public ModuleVH(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvModuleTitle);
            viewPager = itemView.findViewById(R.id.vpModulePages);
            ivLeft = itemView.findViewById(R.id.ivLeft);
            ivRight = itemView.findViewById(R.id.ivRight);
        }
    }


    /* =====================================================================
     * 三页 ViewPager Adapter
     * ===================================================================== */
    static class ModulePageAdapter extends RecyclerView.Adapter<ModulePageAdapter.PageVH> {

        private final DashboardModule module;
        private final Context context;

        ModulePageAdapter(DashboardModule module, Context ctx) {
            this.module = module;
            this.context = ctx;
        }

        @NonNull
        @Override
        public PageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_dashboard_page_container, parent, false);
            return new PageVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PageVH holder, int position) {

            holder.container.removeAllViews();

            switch (position) {
                case 0: // ----- 原始指标 -----
                    fillMetricList(holder.container, module.rawMetrics);
                    break;

                case 1: // ----- 二级指标 -----
                    fillMetricList(holder.container, module.derivedMetrics);
                    break;

                case 2: // ----- 图表页 -----
                    View chartPage = LayoutInflater.from(context)
                            .inflate(R.layout.item_dashboard_chart, holder.container, false);

                    fillChart(chartPage, module.chartData);

                    holder.container.addView(chartPage);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }


        /* =====================================================================
         * Page1 + Page2：填充指标列表
         * ===================================================================== */
        private void fillMetricList(LinearLayout container, List<DashboardMetric> metrics) {
            LayoutInflater inflater = LayoutInflater.from(context);
            for (DashboardMetric m : metrics) {
                View item = inflater.inflate(R.layout.item_dashboard_metric, container, false);
                ((TextView) item.findViewById(R.id.tvMetricTitle)).setText(m.title);
                ((TextView) item.findViewById(R.id.tvMetricValue)).setText(m.value);
                container.addView(item);
            }
        }

        /* =====================================================================
         * Page3：图表页
         * ===================================================================== */
        private void fillChart(View itemView, ChartData chartData) {

            Spinner spSelector = itemView.findViewById(R.id.spChartSelector);
            FrameLayout chartContainer = itemView.findViewById(R.id.chartContainer);

            String[] options = {
                    "首次点击占比（饼图）",
                    "有效停留 vs 无效停留（饼图）",
                    "过去 7 天进入次数（折线图）",
                    "过去 7 天平均有效停留（柱状图）",
                    "昨日各小时进入次数（柱状图）"
            };

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    itemView.getContext(),
                    android.R.layout.simple_spinner_item,
                    options
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSelector.setAdapter(adapter);

            spSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                    chartContainer.removeAllViews();

                    switch (pos) {
                        case 0:
                            loadFirstClickPieChart(chartContainer, chartData, itemView);
                            break;
                        case 1:
                            loadValidStayPieChart(chartContainer, chartData, itemView);
                            break;
                        case 2:
                            loadEnterLineChart(chartContainer, chartData, itemView);
                            break;
                        case 3:
                            loadAvgValidStayBar(chartContainer, chartData, itemView);
                            break;
                        case 4:
                            loadEnterHourBar(chartContainer, chartData, itemView);
                            break;
                    }
                }

                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        /* =====================================================================
         * Pager 内的 ViewHolder
         * ===================================================================== */
        static class PageVH extends RecyclerView.ViewHolder {
            LinearLayout container;
            public PageVH(@NonNull View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.pageContainer);
            }
        }
    }


    /* =====================================================================
     * 图表渲染函数（适用于 Page3）
     * ===================================================================== */

    private static void loadFirstClickPieChart(FrameLayout container, ChartData data, View root) {

        View v = LayoutInflater.from(root.getContext())
                .inflate(R.layout.item_dashboard_chart_pie, container, false);

        PieChart chart = v.findViewById(R.id.pieChart);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(data.clickFriend, "好友消息"));
        entries.add(new PieEntry(data.clickSystem, "系统消息"));
        entries.add(new PieEntry(data.clickPlus, "右上角加号"));
        entries.add(new PieEntry(data.clickSearch, "搜索"));
        entries.add(new PieEntry(data.clickNegative, "左上负一屏"));

        PieDataSet set = new PieDataSet(entries, "");
        int[] colors = {
                Color.parseColor("#4CAF50"), // 绿色
                Color.parseColor("#FFC107"), // 黄色
                Color.parseColor("#F44336"), // 红色
                Color.parseColor("#2196F3"), // 蓝色
                Color.parseColor("#9C27B0")  // 紫色
        };
        set.setColors(colors);
        set.setValueTextSize(12f);

        chart.setData(new PieData(set));
        stylePieChart(chart);
        chart.invalidate();

        container.addView(v);
    }

    private static void loadValidStayPieChart(FrameLayout container, ChartData data, View root) {

        View v = LayoutInflater.from(root.getContext())
                .inflate(R.layout.item_dashboard_chart_pie, container, false);

        PieChart chart = v.findViewById(R.id.pieChart);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(data.validStayCount, "有效停留"));
        entries.add(new PieEntry(data.invalidStayCount, "无效停留"));

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.JOYFUL_COLORS);
        set.setValueTextSize(12f);

        chart.setData(new PieData(set));
        stylePieChart(chart);
        chart.invalidate();

        container.addView(v);
    }

    private static void loadEnterLineChart(FrameLayout container, ChartData data, View root) {

        View v = LayoutInflater.from(root.getContext())
                .inflate(R.layout.item_dashboard_chart_line, container, false);

        LineChart chart = v.findViewById(R.id.lineChart);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.enterCountLast7Days.size(); i++) {
            entries.add(new Entry(i, data.enterCountLast7Days.get(i)));
        }

        LineDataSet set = new LineDataSet(entries, "进入次数趋势");
        set.setColor(Color.BLUE);
        set.setCircleColor(Color.RED);
        set.setValueTextSize(10f);

        chart.setData(new LineData(set));
        styleLineChart(chart);
        chart.invalidate();

        container.addView(v);
    }

    private static void loadAvgValidStayBar(FrameLayout container, ChartData data, View root) {

        View v = LayoutInflater.from(root.getContext())
                .inflate(R.layout.item_dashboard_chart_bar, container, false);

        BarChart chart = v.findViewById(R.id.barChart);

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.avgValidStayLast7Days.size(); i++) {
            entries.add(new BarEntry(i, data.avgValidStayLast7Days.get(i)));
        }

        BarDataSet set = new BarDataSet(entries, "平均有效停留时长（秒）");
        set.setColor(Color.parseColor("#4CAF50"));

        chart.setData(new BarData(set));
        styleBarChart(chart);
        chart.invalidate();

        container.addView(v);
    }

    private static void loadEnterHourBar(FrameLayout container, ChartData data, View root) {

        View v = LayoutInflater.from(root.getContext())
                .inflate(R.layout.item_dashboard_chart_bar, container, false);

        BarChart chart = v.findViewById(R.id.barChart);

        List<BarEntry> entries = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            entries.add(new BarEntry(hour, data.enterCountByHour.get(hour)));
        }

        BarDataSet set = new BarDataSet(entries, "昨日每小时进入次数");
        set.setColor(Color.parseColor("#03A9F4"));

        chart.setData(new BarData(set));
        styleBarChart(chart);
        chart.invalidate();

        container.addView(v);
    }

    private static void stylePieChart(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(60f);
        chart.setEntryLabelColor(Color.DKGRAY);
        chart.setEntryLabelTextSize(12f);
        chart.getDescription().setEnabled(false);

        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setTextSize(12f);
    }

    private static void styleBarChart(BarChart chart) {

        chart.getDescription().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextSize(12f);
        x.setGridColor(Color.parseColor("#E6E6E6"));

        YAxis left = chart.getAxisLeft();
        left.setTextColor(Color.DKGRAY);
        left.setGridColor(Color.parseColor("#F0F0F0"));

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextSize(12f);
    }


    private static void styleLineChart(LineChart chart) {

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        XAxis x = chart.getXAxis();
        x.setTextColor(Color.DKGRAY);
        x.setTextSize(12f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGridColor(Color.parseColor("#DDDDDD"));

        YAxis left = chart.getAxisLeft();
        left.setTextColor(Color.DKGRAY);
        left.setGridColor(Color.parseColor("#EEEEEE"));

        chart.getAxisRight().setEnabled(false);

        chart.getLegend().setTextSize(12f);
        chart.getLegend().setForm(Legend.LegendForm.LINE);
    }

}



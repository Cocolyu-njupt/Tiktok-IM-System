package com.bytedance.firstDemo.feature.negative;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.firstDemo.R;
import com.bytedance.firstDemo.feature.negative.data.WeatherResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 全新未来 4 日天气预报适配器（WeatherDetailActivity 专用）
 * 不影响旧 ForecastAdapter
 */
public class ForecastFullAdapter extends RecyclerView.Adapter<ForecastFullAdapter.VH> {

    private final List<WeatherResponse.Casts> data = new ArrayList<>();

    public void submit(List<WeatherResponse.Casts> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_full, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        WeatherResponse.Casts cast = data.get(position);

        // 星期
        h.tvWeek.setText(toWeekText(cast.week));

        // 日期（MM-dd）
        String date = cast.date;
        if (date != null && date.length() >= 10) {
            h.tvDate.setText(date.substring(5));
        } else {
            h.tvDate.setText("--");
        }

        // 天气
        h.tvWeather.setText(cast.dayweather == null ? "--" : cast.dayweather);

        // 温度范围
        String high = safe(cast.daytemp);
        String low = safe(cast.nighttemp);
        h.tvTemp.setText(high + "°  /  " + low + "°");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    /* ------------ 工具函数 ------------ */

    private String safe(String s) {
        return s == null ? "--" : s;
    }

    private String toWeekText(String w) {
        if (w == null) return "--";
        switch (w) {
            case "1": return "周一";
            case "2": return "周二";
            case "3": return "周三";
            case "4": return "周四";
            case "5": return "周五";
            case "6": return "周六";
            case "7": return "周日";
        }
        return "--";
    }


    /* ------------ ViewHolder ------------ */

    static class VH extends RecyclerView.ViewHolder {
        TextView tvWeek, tvDate, tvWeather, tvTemp;

        VH(@NonNull View itemView) {
            super(itemView);
            tvWeek = itemView.findViewById(R.id.tvWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeather = itemView.findViewById(R.id.tvWeather);
            tvTemp = itemView.findViewById(R.id.tvTemp);
        }
    }
}

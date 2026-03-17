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
 * 未来天气预报 Adapter（列表）
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.VH> {

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
                .inflate(R.layout.item_forecast, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        WeatherResponse.Casts c = data.get(position);

        // date: yyyy-MM-dd
        String date = c.date == null ? "" : c.date;
        String week = toWeekText(c.week);

        h.tvWeek.setText(week);
        h.tvDate.setText(date.substring(5)); // 取 MM-dd
        h.tvWeather.setText(c.dayweather == null ? "--" : c.dayweather);
        h.tvTemp.setText(
                safe(c.daytemp) + "° / " + safe(c.nighttemp) + "°"
        );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvWeek, tvDate, tvWeather, tvTemp;
        VH(@NonNull View itemView) {
            super(itemView);
            tvWeek = itemView.findViewById(R.id.tvWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeather = itemView.findViewById(R.id.tvForecastWeather);
            tvTemp = itemView.findViewById(R.id.tvForecastTemp);
        }
    }

    private String safe(String s) {
        return s == null ? "--" : s;
    }

    private String toWeekText(String w) {
        if (w == null) return "";
        switch (w) {
            case "1": return "周一";
            case "2": return "周二";
            case "3": return "周三";
            case "4": return "周四";
            case "5": return "周五";
            case "6": return "周六";
            case "7": return "周日";
            default: return "周" + w;
        }
    }
}

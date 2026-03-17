package com.bytedance.firstDemo.feature.negative.data;

import java.util.List;

/**
 * UI 显示专用的数据结构
 * 扩展字段：白天/夜间详情（天气、温度、风力）
 * 不删除旧字段，不影响原逻辑
 */
public class WeatherUiState {

    // ===== 保留原有字段 =====
    public String cityName;
    public String currentTemp;
    public String weatherText;
    public String tempHigh;
    public String tempLow;
    public String reportTime;
    public List<WeatherResponse.Casts> forecastList;


    // ===== 新增字段（用于 WeatherDetailActivity） =====
    public String dayWeather;   // 白天天气
    public String dayTemp;      // 白天温度
    public String dayWind;      // 白天风力

    public String nightWeather; // 夜间天气
    public String nightTemp;    // 夜间温度
    public String nightWind;    // 夜间风力


    // ===== 工厂方法：从 WeatherResponse 构造 UIState =====
    public static WeatherUiState from(WeatherResponse resp) {
        WeatherUiState ui = new WeatherUiState();

        if (resp == null || resp.forecasts == null || resp.forecasts.isEmpty()) {
            return ui;
        }

        WeatherResponse.Forecasts f = resp.forecasts.get(0);

        ui.cityName = f.city;                     // 城市名
        ui.reportTime = f.reporttime;             // 发布时间

        // 当前温度 = 白天温度（高德提供的 data 中通常 daytemp 更接近“当前”）
        if (f.casts != null && !f.casts.isEmpty()) {

            WeatherResponse.Casts dayCast = f.casts.get(0); // 第一天 = 今天

            ui.currentTemp = safe(dayCast.daytemp) + "°";
            ui.weatherText = safe(dayCast.dayweather);
            ui.tempHigh = safe(dayCast.daytemp);
            ui.tempLow = safe(dayCast.nighttemp);

            ui.forecastList = f.casts; // 未来预报列表（最多 4 天）


            // ====== 白天卡片信息 ======
            ui.dayWeather = safe(dayCast.dayweather);
            ui.dayTemp = safe(dayCast.daytemp) + "°";
            ui.dayWind = safe(dayCast.daywind) + "风  " + safe(dayCast.daypower) + "级";


            // ====== 夜间卡片信息 ======
            ui.nightWeather = safe(dayCast.nightweather);
            ui.nightTemp = safe(dayCast.nighttemp) + "°";
            ui.nightWind = safe(dayCast.nightwind) + "风  " + safe(dayCast.nightpower) + "级";
        }

        return ui;
    }


    private static String safe(String s) {
        return (s == null || s.length() == 0) ? "--" : s;
    }
}

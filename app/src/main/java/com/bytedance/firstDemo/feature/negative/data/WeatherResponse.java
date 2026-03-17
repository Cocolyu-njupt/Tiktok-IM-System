package com.bytedance.firstDemo.feature.negative.data;

import java.util.List;

/**
 * 高德天气返回（extensions=all）核心字段
 */
public class WeatherResponse {
    public String status;     // "1" 成功
    public String info;       // "OK"
    public String infocode;

    public List<Forecasts> forecasts;

    public static class Forecasts {
        public String city;
        public String adcode;
        public String reporttime;
        public List<Casts> casts;
    }

    public static class Casts {
        public String date;
        public String week;
        public String dayweather;
        public String nightweather;
        public String daytemp;
        public String nighttemp;
        public String daywind;
        public String nightwind;
        public String daypower;
        public String nightpower;
    }
}

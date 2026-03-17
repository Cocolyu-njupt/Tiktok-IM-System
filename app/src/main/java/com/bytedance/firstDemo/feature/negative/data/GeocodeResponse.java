package com.bytedance.firstDemo.feature.negative.data;

import java.util.List;

/**
 * 高德地理编码返回（简化版）
 */
public class GeocodeResponse {
    public String status; // "1" 成功
    public String info;
    public List<Geocode> geocodes;

    public static class Geocode {
        public String adcode;  // ✅ 我们需要的城市 adcode
        public String city;
        public String province;
        public String formatted_address;
    }
}

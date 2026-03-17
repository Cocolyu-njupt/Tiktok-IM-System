package com.bytedance.firstDemo.feature.negative.net;

import com.bytedance.firstDemo.feature.negative.data.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("v3/weather/weatherInfo")
    Call<WeatherResponse> getWeather(
            @Query("city") String cityCode,
            @Query("extensions") String extensions, // "all"
            @Query("key") String key
    );
}

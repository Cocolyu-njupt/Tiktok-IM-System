package com.bytedance.firstDemo.feature.negative.data;

import com.bytedance.firstDemo.feature.negative.net.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    public interface RepoCallback {
        void onSuccess(WeatherResponse resp);
        void onError(String msg);
    }

    public interface AdcodeCallback {
        void onSuccess(String adcode);
        void onError(String msg);
    }

    // TODO: 填你的高德 Key
    private static final String AMAP_KEY = "375a8f67801f074db641fd3662523738";

    // ===== 1) 拉天气（网络）=====
    public void fetchWeather(String cityCode, RepoCallback cb) {
        Call<WeatherResponse> call = RetrofitClient.weatherApi()
                .getWeather(cityCode, "all", AMAP_KEY);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onError("网络错误: " + response.code());
                    return;
                }

                WeatherResponse body = response.body();
                if (!"1".equals(body.status)) {
                    cb.onError(body.info == null ? "请求失败" : body.info);
                    return;
                }

                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    // ===== 2) 城市名 -> adcode（高德地理编码 网络）=====
    public void fetchAdcodeByCityName(String cityName, AdcodeCallback cb) {

        RetrofitClient.geocodeApi()
                .geocode(cityName, AMAP_KEY)
                .enqueue(new retrofit2.Callback<GeocodeResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<GeocodeResponse> call,
                            retrofit2.Response<GeocodeResponse> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null) {
                            cb.onError("城市解析失败: " + response.code());
                            return;
                        }

                        GeocodeResponse body = response.body();
                        if (!"1".equals(body.status)
                                || body.geocodes == null
                                || body.geocodes.isEmpty()
                                || body.geocodes.get(0).adcode == null) {
                            cb.onError(body.info == null ? "城市解析失败" : body.info);
                            return;
                        }

                        cb.onSuccess(body.geocodes.get(0).adcode);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<GeocodeResponse> call, Throwable t) {
                        cb.onError(t.getMessage());
                    }
                });
    }
}

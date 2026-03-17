package com.bytedance.firstDemo.feature.negative.net;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.bytedance.firstDemo.feature.negative.net.GeocodeApiService;


public class RetrofitClient {

    private static volatile Retrofit retrofit;

    private static final String BASE_URL = "https://restapi.amap.com/";

    public static Retrofit get() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {

                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient okHttp = new OkHttpClient.Builder()
                            .addInterceptor(log)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttp)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static WeatherApiService weatherApi() {
        return get().create(WeatherApiService.class);
    }

    public static GeocodeApiService geocodeApi() {
        return get().create(GeocodeApiService.class);
    }


}

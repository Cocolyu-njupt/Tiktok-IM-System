package com.bytedance.firstDemo.feature.negative.net;

import com.bytedance.firstDemo.feature.negative.data.GeocodeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodeApiService {

    @GET("v3/geocode/geo")
    Call<GeocodeResponse> geocode(
            @Query("address") String cityName,
            @Query("key") String key
    );
}

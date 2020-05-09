package com.kissasian.sh.network.apis;

import com.kissasian.sh.models.home_content.HomeContent;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface HomeContentApi {

    @GET("home_content_for_android")
    Call<HomeContent> getHomeContent(@Header("API-KEY") String apiKey);
}

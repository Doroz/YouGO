package com.shedulerforevents.client;

import com.shedulerforevents.model.Response;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Usuario on 06/11/2016.
 */

public interface ApiService {

    public static final String URL = "http://api.openweathermap.org/data/2.5/";


    @GET("weather")
Call<Response> getWheater(@Query("q") String q, @Query("appid") String appid, @Query("units") String units, @Query("lang") String lang);


}

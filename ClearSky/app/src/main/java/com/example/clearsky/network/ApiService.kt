package com.example.clearsky.network

import com.example.clearsky.model.CityResponseApi
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("unit") unit:String,
        @Query("appid") ApiKey:String
        ):CurrentResponseApi

    @GET("data/2.5/forecast")
    suspend fun getForecastWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("unit") unit:String,
        @Query("appid") ApiKey:String
    ): ForecastResponseApi

    @GET("geo/1.0/direct")
    fun getCitiesList(
        @Query("q") q:String,
        @Query("limit") limit:Int,
        @Query("appid") ApiKey:String
    ): CityResponseApi

}
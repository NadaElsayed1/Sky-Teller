package com.example.clearsky.network

import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi

interface IRemoteDataStructure {
    suspend fun fetchCurrentWeather(lat: Double, lng: Double, unit: String, lang : String): CurrentResponseApi

    suspend fun fetchForecastWeather(lat: Double, lng: Double, unit: String, lang : String): ForecastResponseApi
}
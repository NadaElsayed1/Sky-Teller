package com.example.clearsky.model.repository

import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.network.IRemoteDataStructure

class FakeRemoteDataStructure(
    private val currentWeather: CurrentResponseApi,
    private val forecastWeather: ForecastResponseApi
) : IRemoteDataStructure {

    override suspend fun fetchCurrentWeather(
        lat: Double,
        lng: Double,
        unit: String,
        lang: String
    ): CurrentResponseApi {
        return currentWeather
    }

    override suspend fun fetchForecastWeather(
        lat: Double,
        lng: Double,
        unit: String,
        lang: String
    ): ForecastResponseApi {
        return forecastWeather
    }
}

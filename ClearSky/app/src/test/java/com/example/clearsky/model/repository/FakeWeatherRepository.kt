package com.example.clearsky.model.repository

import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi

class FakeWeatherRepository : IWeatherRepository {

    var currentWeatherData: CurrentResponseApi? = null
    var forecastWeatherData: ForecastResponseApi? = null

    override suspend fun getCurrentWeather(lat: Double, lng: Double, unit: String, lang: String): CurrentResponseApi {
        return currentWeatherData ?: throw Exception("Current weather data not set")
    }

    override suspend fun getForecastWeather(lat: Double, lng: Double, unit: String, lang: String): ForecastResponseApi {
        return forecastWeatherData ?: throw Exception("Forecast weather data not set")
    }

    override suspend fun getFavorites(): List<CurrentResponseApi> = emptyList()

    override fun addFavorite(city: CurrentResponseApi) {}

    override fun removeFavorite(city: CurrentResponseApi) {}
}

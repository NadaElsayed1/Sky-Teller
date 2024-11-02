package com.example.clearsky.model.repository

import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi

interface IWeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lng: Double, unit: String, lang: String): CurrentResponseApi

    suspend fun getForecastWeather(lat: Double, lng: Double, unit: String, lang: String): ForecastResponseApi

    suspend fun getFavorites(): List<CurrentResponseApi>
    fun addFavorite(city: CurrentResponseApi)
    fun removeFavorite(city: CurrentResponseApi)
}

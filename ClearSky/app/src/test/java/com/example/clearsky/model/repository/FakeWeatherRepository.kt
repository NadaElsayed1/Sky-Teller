package com.example.clearsky.model.repository

import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWeatherRepository : IWeatherRepository {

    var currentWeatherData: CurrentResponseApi? = null
    var forecastWeatherData: ForecastResponseApi? = null

    private val _favoritesFlow = MutableStateFlow<List<CurrentResponseApi>>(emptyList())
    val favoritesFlow = _favoritesFlow.asStateFlow()

    override suspend fun getCurrentWeather(lat: Double, lng: Double, unit: String, lang: String): CurrentResponseApi {
        return currentWeatherData ?: throw Exception("Current weather data not set")
    }

    override suspend fun getForecastWeather(lat: Double, lng: Double, unit: String, lang: String): ForecastResponseApi {
        return forecastWeatherData ?: throw Exception("Forecast weather data not set")
    }

    override suspend fun getFavorites(): Flow<List<CurrentResponseApi>> {
        return favoritesFlow
    }

    override fun addFavorite(city: CurrentResponseApi) {
        _favoritesFlow.value = _favoritesFlow.value + city
    }

    override fun removeFavorite(city: CurrentResponseApi) {
        _favoritesFlow.value = _favoritesFlow.value - city
    }
}

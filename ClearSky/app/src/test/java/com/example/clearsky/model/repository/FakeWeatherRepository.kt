package com.example.clearsky.model.repository

import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi

class FakeWeatherRepository : IWeatherRepository {
    override suspend fun getCurrentWeather(
        lat: Double,
        lng: Double,
        unit: String,
        lang: String
    ): CurrentResponseApi {
        TODO("Not yet implemented")
    }

    override suspend fun getForecastWeather(
        lat: Double,
        lng: Double,
        unit: String,
        lang: String
    ): ForecastResponseApi {
        TODO("Not yet implemented")
    }

    override suspend fun getFavorites(): List<CurrentResponseApi> {
        TODO("Not yet implemented")
    }

    override fun addFavorite(city: CurrentResponseApi) {
        TODO("Not yet implemented")
    }

    override fun removeFavorite(city: CurrentResponseApi) {
        TODO("Not yet implemented")
    }
}
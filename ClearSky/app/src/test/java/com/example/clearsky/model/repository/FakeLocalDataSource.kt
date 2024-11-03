package com.example.clearsky.model.repository

import com.example.clearsky.db.IWeatherLocalDataSource
import com.example.clearsky.model.CurrentResponseApi

class FakeLocalDataSource(private val favWeather: MutableList<CurrentResponseApi> = mutableListOf()) : IWeatherLocalDataSource {

    override suspend fun getFavorites(): List<CurrentResponseApi> {
        return favWeather.toList()
    }

    override fun addFavorite(city: CurrentResponseApi) {
        favWeather.add(city)
    }

    override fun removeFavorite(city: CurrentResponseApi) {
        favWeather.remove(city)
    }
}

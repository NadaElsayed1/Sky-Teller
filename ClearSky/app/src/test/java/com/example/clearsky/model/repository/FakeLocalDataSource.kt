package com.example.clearsky.model.repository

import com.example.clearsky.db.IWeatherLocalDataSource
import com.example.clearsky.model.CurrentResponseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLocalDataSource(private val favWeather: MutableList<CurrentResponseApi> = mutableListOf()) : IWeatherLocalDataSource {

    // Create a StateFlow to hold the list of favorites
    private val _favoritesFlow = MutableStateFlow(favWeather.toList())
    val favoritesFlow = _favoritesFlow.asStateFlow()

    override suspend fun getFavorites(): Flow<List<CurrentResponseApi>> {
        return favoritesFlow
    }

    override fun addFavorite(city: CurrentResponseApi) {
        favWeather.add(city)
        _favoritesFlow.value = favWeather.toList() // Update the StateFlow
    }

    override fun removeFavorite(city: CurrentResponseApi) {
        favWeather.remove(city)
        _favoritesFlow.value = favWeather.toList() // Update the StateFlow
    }
}

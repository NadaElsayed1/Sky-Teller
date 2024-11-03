package com.example.clearsky.db

import com.example.clearsky.model.CurrentResponseApi

interface IWeatherLocalDataSource {
    // New methods for managing favorites
    suspend fun getFavorites(): List<CurrentResponseApi>
    fun addFavorite(city: CurrentResponseApi)
    fun removeFavorite(city: CurrentResponseApi)
}
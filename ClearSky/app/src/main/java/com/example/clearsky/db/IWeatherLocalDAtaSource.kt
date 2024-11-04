package com.example.clearsky.db
import com.example.clearsky.model.CurrentResponseApi

interface IWeatherLocalDataSource {
    suspend fun getFavorites(): List<CurrentResponseApi>
    fun addFavorite(city: CurrentResponseApi)
    fun removeFavorite(city: CurrentResponseApi)
}
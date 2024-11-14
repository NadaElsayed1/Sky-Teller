package com.example.clearsky.db
import com.example.clearsky.model.CurrentResponseApi
import kotlinx.coroutines.flow.Flow

interface IWeatherLocalDataSource {
    suspend fun getFavorites(): Flow<List<CurrentResponseApi>>
    fun addFavorite(city: CurrentResponseApi)
    fun removeFavorite(city: CurrentResponseApi)
}
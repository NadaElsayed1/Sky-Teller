package com.example.clearsky.model.repository

import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.network.RemoteDataStructure

class WeatherRepository(private val remoteDataStructure: RemoteDataStructure,private val localDataSource: WeatherLocalDataSource) {

    suspend fun getCurrentWeather(lat: Double, lng: Double, unit: String) =
        remoteDataStructure.fetchCurrentWeather(lat, lng, unit)

    suspend fun getForecastWeather(lat: Double, lng: Double, unit: String) =
        remoteDataStructure.fetchForecastWeather(lat, lng, unit)

    suspend fun getFavorites(): List<CurrentResponseApi> {
        return localDataSource.getFavorites()
    }

    fun addFavorite(city: CurrentResponseApi) {
        localDataSource.addFavorite(city)
    }

    fun removeFavorite(city: CurrentResponseApi) {
        localDataSource.removeFavorite(city)
    }
}

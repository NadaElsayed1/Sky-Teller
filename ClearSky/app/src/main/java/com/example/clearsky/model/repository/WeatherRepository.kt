package com.example.clearsky.model.repository

import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.network.RemoteDataStructure

class WeatherRepository(private val remoteDataStructure: RemoteDataStructure, private val localDataSource: WeatherLocalDataSource) :
    IWeatherRepository {

    override suspend fun getCurrentWeather(lat: Double, lng: Double, unit: String, lang: String) =
        remoteDataStructure.fetchCurrentWeather(lat, lng, unit,lang)

    override suspend fun getForecastWeather(lat: Double, lng: Double, unit: String, lang: String) =
        remoteDataStructure.fetchForecastWeather(lat, lng, unit,lang)

    override suspend fun getFavorites(): List<CurrentResponseApi> {
        return localDataSource.getFavorites()
    }

    override fun addFavorite(city: CurrentResponseApi) {
        localDataSource.addFavorite(city)
    }

    override fun removeFavorite(city: CurrentResponseApi) {
        localDataSource.removeFavorite(city)
    }
}

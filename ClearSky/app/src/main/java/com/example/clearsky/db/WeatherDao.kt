package com.example.clearsky.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_forecast WHERE city = :cityName")
    fun getWeatherForecast(cityName: String): Flow<List<ForecastResponseApi>>

    @Query("SELECT * FROM current_weather WHERE name = :cityName")
    fun getWeatherByCity(cityName: String): Flow<List<CurrentResponseApi>>

    @Query("SELECT * FROM current_weather")
    fun getFavorites(): Flow<List<CurrentResponseApi>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorite(city: CurrentResponseApi)

    @Delete
    fun removeFavorite(city: CurrentResponseApi)
}


package com.example.clearsky.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherForecast(weatherList: List<ForecastResponseApi>)

    @Query("SELECT * FROM weather_forecast WHERE city = :cityName")
    fun getWeatherForecast(cityName: String): LiveData<List<ForecastResponseApi>>

    @Query("SELECT * FROM current_weather WHERE name = :cityName")
    fun getWeatherByCity(cityName: String): LiveData<List<CurrentResponseApi>>

    @Query("SELECT * FROM current_weather WHERE coord = :coord")
    fun getWeatherByCoordinates(coord: String): LiveData<List<CurrentResponseApi>>
}

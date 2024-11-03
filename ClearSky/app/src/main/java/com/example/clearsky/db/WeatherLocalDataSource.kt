package com.example.clearsky.db

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherLocalDataSource private constructor(context: Context) : IWeatherLocalDataSource {
    private val weatherDao: WeatherDao
    val storedForecasts: LiveData<List<ForecastResponseApi>>
    val storedCurrentWeather: LiveData<List<CurrentResponseApi>>

    init {
        val database = WeatherDatabase.getDatabase(context.applicationContext)
        weatherDao = database.weatherDao()
        storedForecasts = weatherDao.getWeatherForecast("Cairo")
        storedCurrentWeather = weatherDao.getWeatherByCity("Cairo")
    }

    companion object {
        @Volatile
        private var instance: WeatherLocalDataSource? = null

        fun getInstance(context: Context): WeatherLocalDataSource {
            return instance ?: synchronized(this) {
                instance ?: WeatherLocalDataSource(context).also { instance = it }
            }
        }
    }

    override suspend fun getFavorites(): List<CurrentResponseApi> {
        return weatherDao.getFavorites()
    }

    override fun addFavorite(city: CurrentResponseApi) {
        CoroutineScope(Dispatchers.IO).launch {
            weatherDao.addFavorite(city)
        }
    }

    override fun removeFavorite(city: CurrentResponseApi) {
        CoroutineScope(Dispatchers.IO).launch {
            weatherDao.removeFavorite(city)
        }
    }
}

//    fun insertForecasts(forecasts: List<ForecastResponseApi>) {
//        CoroutineScope(Dispatchers.IO).launch {
//            weatherDao.insertWeatherForecast(forecasts)
//        }
//    }
//
//    fun deleteCurrentWeather(currentWeather: CurrentResponseApi) {
//        CoroutineScope(Dispatchers.IO).launch {
//            weatherDao.deleteWeather(currentWeather)
//        }
//    }
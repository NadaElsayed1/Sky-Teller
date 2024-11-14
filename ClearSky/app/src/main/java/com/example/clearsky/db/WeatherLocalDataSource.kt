package com.example.clearsky.db

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WeatherLocalDataSource(
    private val weatherDao: WeatherDao
) : IWeatherLocalDataSource {


    companion object {
        @Volatile
        private var instance: WeatherLocalDataSource? = null

        fun getInstance(context: Context): WeatherLocalDataSource {
            return instance ?: synchronized(this) {
                instance ?: WeatherLocalDataSource(WeatherDatabase.getDatabase(context).weatherDao()).also {
                    instance = it
                }
            }
        }
    }

    override suspend fun getFavorites(): Flow<List<CurrentResponseApi>> {
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

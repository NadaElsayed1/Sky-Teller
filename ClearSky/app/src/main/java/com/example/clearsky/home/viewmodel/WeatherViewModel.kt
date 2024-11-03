package com.example.clearsky.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.model.repository.IWeatherRepository
import com.example.clearsky.model.repository.WeatherRepository
import kotlinx.coroutines.launch


class WeatherViewModel(private val repository: IWeatherRepository) : ViewModel() {

    private val _currentWeatherData = MutableLiveData<CurrentResponseApi>()
    val currentWeatherData: LiveData<CurrentResponseApi> get() = _currentWeatherData

    private val _forecastWeatherData = MutableLiveData<List<ForecastResponseApi.Forecast>>()
    val forecastWeatherData: LiveData<List<ForecastResponseApi.Forecast>> get() = _forecastWeatherData

    // LiveData for favorites
    private val _favorites = MutableLiveData<List<CurrentResponseApi>>()
    val favorites: LiveData<List<CurrentResponseApi>> get() = _favorites

    fun fetchFavorites() {
        viewModelScope.launch {
            val favoritesList = repository.getFavorites()
            _favorites.postValue(favoritesList)
        }
    }
    // Function to fetch current weather data
    fun fetchCurrentWeather(lat: Double, lng: Double, unit: String, lang: String) {
        viewModelScope.launch {
            try {
                val currentWeather = repository.getCurrentWeather(lat, lng, unit, lang)
                _currentWeatherData.postValue(currentWeather)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching current weather data: ${e.message}")
            }
        }
    }

    // Function to fetch forecast weather data
    fun fetchForecastWeather(lat: Double, lng: Double, unit: String, lang: String) {
        viewModelScope.launch {
            try {
                val forecastWeather = repository.getForecastWeather(lat, lng, unit, lang)
                _forecastWeatherData.postValue(forecastWeather.forecastList)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching forecast weather data: ${e.message}")
            }
        }
    }
//    fun getFavorites(): List<CurrentResponseApi> {
//        return repository.getFavorites() // Make sure this method returns a List<CurrentResponseApi>
//    }
//     Function to add a city to favorites
    fun addFavorite(city: CurrentResponseApi) {
        viewModelScope.launch {
            try {
                repository.addFavorite(city)
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error adding favorite city: ${e.message}")
            }
        }
    }
//fun addFavorite(city: CurrentResponseApi) {
//    repository.addFavorite(city) // Ensure this method exists in the repository
//}

    // Function to remove a city from favorites
    fun removeFavorite(city: CurrentResponseApi) {
        viewModelScope.launch {
            try {
                repository.removeFavorite(city)
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error removing favorite city: ${e.message}")
            }
        }
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

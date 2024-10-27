package com.example.clearsky.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.network.ApiClient
import com.example.clearsky.network.ApiService
import com.example.clearsky.model.repository.WeatherRepository
import kotlinx.coroutines.launch

//class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
//
//    private val _currentWeatherData = MutableLiveData<ForecastResponseApi>()
//    val currentWeatherData: LiveData<ForecastResponseApi> get() = _currentWeatherData
//
//    private val _forecastWeatherData = MutableLiveData<List<ForecastResponseApi.Forecast>>()
//    val forecastWeatherData: LiveData<List<ForecastResponseApi.Forecast>> get() = _forecastWeatherData
//
//    // Function to fetch current weather data
//    fun fetchCurrentWeather(lat: Double, lng: Double, unit: String) {
//        viewModelScope.launch {
//            try {
//                val currentWeather = repository.getCurrentWeather(lat, lng, unit)
//                _currentWeatherData.postValue(currentWeather)
//            } catch (e: Exception) {
//                Log.e("WeatherViewModel", "Error fetching current weather data: ${e.message}")
//            }
//        }
//    }
//
//    // Function to fetch forecast weather data
//    fun fetchForecastWeather(lat: Double, lng: Double, unit: String) {
//        viewModelScope.launch {
//            try {
//                val forecastWeather = repository.getForecastWeather(lat, lng, unit)
//                _forecastWeatherData.postValue(forecastWeather.forecastList)
//            } catch (e: Exception) {
//                Log.e("WeatherViewModel", "Error fetching forecast weather data: ${e.message}")
//            }
//        }
//    }
//}
//
//class WeatherViewModelFactory(
//    private val repository: WeatherRepository
//) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
//            return WeatherViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}


class WeatherViewModel(val repository: WeatherRepository): ViewModel() {

    constructor():this(WeatherRepository(ApiClient().getClient().create(ApiService::class.java)))

    fun loadCurrentWeather(lat:Double,lng:Double,unit:String)=
        repository.getCurrentWeather(lat,lng,unit)

    fun loadForecastWeather(lat:Double,lng:Double,unit:String)=
        repository.getForecastWeather(lat,lng,unit)
}
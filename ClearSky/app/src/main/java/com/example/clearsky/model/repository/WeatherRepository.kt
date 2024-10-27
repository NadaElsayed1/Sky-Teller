package com.example.clearsky.model.repository

import com.example.clearsky.db.WeatherDao
import com.example.clearsky.network.ApiService

class WeatherRepository(val api: ApiService) {

    companion object {
        private const val API_KEY = "9832068f1c229dbbef08a89208bb2d8f"
    }

    fun getCurrentWeather(lat:Double, lng:Double, unit:String)=
        api.getCurrentWeather(lat,lng,unit,API_KEY)

    fun getForecastWeather(lat:Double, lng:Double, unit:String)=
        api.getForecastWeather(lat,lng,unit,API_KEY)
}
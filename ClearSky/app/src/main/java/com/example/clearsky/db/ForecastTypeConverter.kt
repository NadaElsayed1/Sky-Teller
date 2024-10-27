package com.example.clearsky.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ForecastTypeConverter {

    private val gson = Gson()

    // Convert List<Forecast> to JSON String
    @TypeConverter
    fun fromForecastList(forecastList: List<ForecastResponseApi.Forecast>?): String? {
        return gson.toJson(forecastList)
    }

    // Convert JSON String back to List<Forecast>
    @TypeConverter
    fun toForecastList(forecastListString: String?): List<ForecastResponseApi.Forecast>? {
        val type = object : TypeToken<List<ForecastResponseApi.Forecast>>() {}.type
        return gson.fromJson(forecastListString, type)
    }

    // Convert City to JSON String
    @TypeConverter
    fun fromCity(city: ForecastResponseApi.City?): String? {
        return gson.toJson(city)
    }

    // Convert JSON String back to City
    @TypeConverter
    fun toCity(cityString: String?): ForecastResponseApi.City? {
        return gson.fromJson(cityString, ForecastResponseApi.City::class.java)
    }

    // Convert Coord to JSON String
    @TypeConverter
    fun fromCoord(coord: ForecastResponseApi.Coord?): String? {
        return gson.toJson(coord)
    }

    // Convert JSON String back to Coord
    @TypeConverter
    fun toCoord(coordString: String?): ForecastResponseApi.Coord? {
        return gson.fromJson(coordString, ForecastResponseApi.Coord::class.java)
    }
}

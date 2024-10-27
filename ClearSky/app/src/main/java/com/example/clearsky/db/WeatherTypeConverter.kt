package com.example.clearsky.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverter {

    private val gson = Gson()

    // Convert Clouds object to JSON string
    @TypeConverter
    fun fromClouds(clouds: CurrentResponseApi.Clouds?): String? {
        return clouds?.let { gson.toJson(it) }
    }

    // Convert JSON string back to Clouds object
    @TypeConverter
    fun toClouds(cloudsString: String?): CurrentResponseApi.Clouds? {
        return cloudsString?.let {
            val type = object : TypeToken<CurrentResponseApi.Clouds>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Convert Coord object to JSON string
    @TypeConverter
    fun fromCoord(coord: CurrentResponseApi.Coord?): String? {
        return coord?.let { gson.toJson(it) }
    }

    // Convert JSON string back to Coord object
    @TypeConverter
    fun toCoord(coordString: String?): CurrentResponseApi.Coord? {
        return coordString?.let {
            val type = object : TypeToken<CurrentResponseApi.Coord>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Convert Main object to JSON string
    @TypeConverter
    fun fromMain(main: CurrentResponseApi.Main?): String? {
        return main?.let { gson.toJson(it) }
    }

    // Convert JSON string back to Main object
    @TypeConverter
    fun toMain(mainString: String?): CurrentResponseApi.Main? {
        return mainString?.let {
            val type = object : TypeToken<CurrentResponseApi.Main>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Convert Rain object to JSON string
    @TypeConverter
    fun fromRain(rain: CurrentResponseApi.Rain?): String? {
        return rain?.let { gson.toJson(it) }
    }

    // Convert JSON string back to Rain object
    @TypeConverter
    fun toRain(rainString: String?): CurrentResponseApi.Rain? {
        return rainString?.let {
            val type = object : TypeToken<CurrentResponseApi.Rain>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Convert Sys object to JSON string
    @TypeConverter
    fun fromSys(sys: CurrentResponseApi.Sys?): String? {
        return sys?.let { gson.toJson(it) }
    }

    // Convert JSON string back to Sys object
    @TypeConverter
    fun toSys(sysString: String?): CurrentResponseApi.Sys? {
        return sysString?.let {
            val type = object : TypeToken<CurrentResponseApi.Sys>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Convert Weather object list to JSON string
    @TypeConverter
    fun fromWeatherList(weather: List<CurrentResponseApi.Weather?>?): String? {
        return weather?.let { gson.toJson(it) }
    }

    // Convert JSON string back to Weather object list
    @TypeConverter
    fun toWeatherList(weatherString: String?): List<CurrentResponseApi.Weather?>? {
        return weatherString?.let {
            val type = object : TypeToken<List<CurrentResponseApi.Weather>>() {}.type
            gson.fromJson(it, type)
        }
    }

    // Convert Wind object to JSON string
    @TypeConverter
    fun fromWind(wind: CurrentResponseApi.Wind?): String? {
        return wind?.let { gson.toJson(it) }
    }

    // Convert JSON string back to Wind object
    @TypeConverter
    fun toWind(windString: String?): CurrentResponseApi.Wind? {
        return windString?.let {
            val type = object : TypeToken<CurrentResponseApi.Wind>() {}.type
            gson.fromJson(it, type)
        }
    }
}

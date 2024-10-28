package com.example.clearsky.db

import androidx.room.TypeConverter
import com.example.clearsky.model.CityResponseApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CityTypeConverter {
    @TypeConverter
    fun fromLocalNames(localNames: CityResponseApi.LocalNames?): String? {
        return localNames?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toLocalNames(data: String?): CityResponseApi.LocalNames? {
        return data?.let {
            val type = object : TypeToken<CityResponseApi.LocalNames>() {}.type
            Gson().fromJson(data, type)
        }
    }
}

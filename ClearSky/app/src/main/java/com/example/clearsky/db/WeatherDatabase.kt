package com.example.clearsky.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.example.clearsky.model.CityResponseApi
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.model.ForecastTypeConverter
import com.example.clearsky.model.WeatherTypeConverter

@Database(entities = [ForecastResponseApi::class, CurrentResponseApi::class, CityResponseApi::class], version = 1)
@TypeConverters(ForecastTypeConverter::class, WeatherTypeConverter::class, CityTypeConverter::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                ) .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

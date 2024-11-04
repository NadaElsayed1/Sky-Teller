package com.example.clearsky.network
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RemoteDataStructure : IRemoteDataStructure {

    private const val BASE_URL = "https://api.openweathermap.org"
    private const val API_KEY = "9832068f1c229dbbef08a89208bb2d8f"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override suspend fun fetchCurrentWeather(lat: Double, lng: Double, unit: String, lang : String): CurrentResponseApi {
        return apiService.getCurrentWeather(lat, lng, unit, API_KEY,lang)
    }

    override suspend fun fetchForecastWeather(lat: Double, lng: Double, unit: String, lang : String): ForecastResponseApi {
        return apiService.getForecastWeather(lat, lng, unit, API_KEY,lang)
    }
}

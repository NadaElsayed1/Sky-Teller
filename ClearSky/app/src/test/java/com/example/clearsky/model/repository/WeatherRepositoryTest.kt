package com.example.clearsky.model.repository

import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.network.RemoteDataStructure
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {
    private lateinit var weatherRepository: WeatherRepository

    // Sample data for testing with all required fields
    private val sampleCurrentWeather = CurrentResponseApi(
        id = 1,
        name = "Cairo",
        base = "stations",
        clouds = CurrentResponseApi.Clouds(all = 90),
        cod = 200,
        coord = CurrentResponseApi.Coord(lat = 30.0444, lon = 31.2357),
        dt = 1618317040,
        main = CurrentResponseApi.Main(
            feelsLike = 305.15,
            grndLevel = 1013,
            humidity = 40,
            pressure = 1016,
            seaLevel = 1013,
            temp = 300.15,
            tempMax = 302.15,
            tempMin = 298.15
        ),
        rain = null,
        sys = CurrentResponseApi.Sys(
            country = "EG",
            id = 1,
            sunrise = 1618282134,
            sunset = 1618330522,
            type = 1
        ),
        timezone = 7200,
        visibility = 10000,
        weather = listOf(
            CurrentResponseApi.Weather(
                description = "clear sky",
                icon = "01d",
                id = 800,
                main = "Clear"
            )
        ),
        wind = CurrentResponseApi.Wind(
            deg = 350,
            gust = 2.5,
            speed = 1.5
        )
    )

    private val sampleForecastWeather = ForecastResponseApi(
        cod = "200",
        message = 0,
        cnt = 1,
        forecastList = listOf(
            ForecastResponseApi.Forecast(
                dt = 1618317040,
                main = ForecastResponseApi.Main(
                    temp = 300.15,
                    feelsLike = 305.15,
                    tempMin = 298.15,
                    tempMax = 302.15,
                    pressure = 1016,
                    seaLevel = 1013,
                    grndLevel = 1013,
                    humidity = 40,
                    tempKf = 0.0
                ),
                weather = listOf(
                    ForecastResponseApi.Weather(
                        id = 800,
                        main = "Clear",
                        description = "clear sky",
                        icon = "01d"
                    )
                ),
                clouds = ForecastResponseApi.Clouds(all = 90),
                wind = ForecastResponseApi.Wind(
                    speed = 1.5,
                    deg = 350,
                    gust = 2.5
                ),
                visibility = 10000,
                pop = 0.0,
                rain = null,
                sys = ForecastResponseApi.Sys(pod = "d"),
                dtTxt = "2021-04-10 12:00:00"
            )
        ),
        city = ForecastResponseApi.City(
            id = 1,
            name = "Cairo",
            coord = ForecastResponseApi.Coord(lat = 30.0444, lon = 31.2357),
            country = "EG",
            population = 2000000,
            timezone = 7200,
            sunrise = 1618282134,
            sunset = 1618330522
        )
    )

    private val remoteDataStructure = FakeRemoteDataStructure(sampleCurrentWeather, sampleForecastWeather)
    private val localDataSource = FakeLocalDataSource(mutableListOf(sampleCurrentWeather))

    @Before
    fun setUp() {
        // Initialize the repository with the fake data sources
        weatherRepository = WeatherRepository(remoteDataStructure, localDataSource)
    }

    @Test
    fun getCurrentWeather_returnsExpectedCurrentWeather() = runTest {
        // When: calling getCurrentWeather on the repository
        val result = weatherRepository.getCurrentWeather(30.0444, 31.2357, "metric", "en")

        // Then: the result should match the expected sample current weather data
        assertThat(result, IsEqual(sampleCurrentWeather))
    }

    @Test
    fun getForecastWeather_returnsExpectedForecastWeather() = runTest {
        // When: calling getForecastWeather on the repository
        val result = weatherRepository.getForecastWeather(30.0444, 31.2357, "metric", "en")

        // Then: the result should match the expected sample forecast weather data
        assertThat(result, IsEqual(sampleForecastWeather))
    }
}

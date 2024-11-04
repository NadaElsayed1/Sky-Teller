package com.example.clearsky.home.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.model.repository.FakeWeatherRepository
import com.example.clearsky.model.repository.IWeatherRepository
import getOrAwaitValue
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
//@Config(sdk = [28])
class WeatherViewModelTest {
    private lateinit var repository: IWeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @get:Rule
    val viewModelRule = ViewModelRule()

    @Before
    fun setup() {
        repository = FakeWeatherRepository()
        viewModel = WeatherViewModel(repository)
    }

    @Test
    fun fetchCurrentWeather_success_updatesLiveData() = runTest {
        // Given: Set up expected data
        val currentWeatherData = CurrentResponseApi(
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

        (repository as FakeWeatherRepository).currentWeatherData = currentWeatherData

        // When: fetchCurrentWeather is called
        viewModel.fetchCurrentWeather(30.0444, 31.2357, "metric", "en")

        // Delay to give time for LiveData to update
        advanceTimeBy(1000)

        // Then: Assert LiveData is updated
        val result = viewModel.currentWeatherData.getOrAwaitValue(time = 5)
        assertThat(result, `is`(currentWeatherData))
    }

    @Test
    fun fetchForecastWeather_success_updatesLiveData() = runTest {
        // Given: Set up expected data
        val forecastWeatherData = ForecastResponseApi(
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

        (repository as FakeWeatherRepository).forecastWeatherData = forecastWeatherData

        // When: fetchForecastWeather is called
        viewModel.fetchForecastWeather(30.0444, 31.2357, "metric", "en")

        // Delay to give time for LiveData to update
        advanceTimeBy(1000)

        // Then: Assert LiveData is updated
        val result = viewModel.forecastWeatherData.getOrAwaitValue(time = 5)
        assertThat(result, `is`(forecastWeatherData.forecastList))
    }
}
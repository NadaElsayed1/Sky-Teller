package com.example.clearsky.db

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class WeatherDaoTest {

    @get:Rule
    val rule = InstantTaskExecutorRule() // force to work in ain thread so should use it allowMainThreadQueries()

    lateinit var database: WeatherDatabase
    lateinit var weatherDao: WeatherDao

    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).allowMainThreadQueries().build()

        weatherDao = database.weatherDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun getWeatherByCity_retrievesSpecificCity() = runTest {
        // Given: Adding a sample city to the favorites
        val cityName = "Cairo"
        val favoriteCity = CurrentResponseApi(
            id = 1,
            name = cityName,
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

        // Insert the city into the favorites
        weatherDao.addFavorite(favoriteCity)

        // When: Retrieving the city by its name
        val result = weatherDao.getWeatherByCity(cityName).first() // Collect the first emitted value

        // Then: Verify that the retrieved city matches the expected favorite city
        val expectedCityList = listOf(favoriteCity)
        assertThat(result, equalTo(expectedCityList))
    }


    @Test
    fun getFavorites_retrievesListOfFavoriteCities() = runTest {
        // Given: Adding sample cities to favorites
        val favoriteCity1 = CurrentResponseApi(
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

        val favoriteCity2 = favoriteCity1.copy(id = 2, name = "Alexandria")

        // Add the cities to the favorites in the database
        weatherDao.addFavorite(favoriteCity1)
        weatherDao.addFavorite(favoriteCity2)

        // When: Retrieving the favorites
        val result = weatherDao.getFavorites().first() // Collect the first emitted value

        // Then: Verify that the result matches the added favorite cities
        val expectedFavoritesList = listOf(favoriteCity1, favoriteCity2)
        assertThat(result, equalTo(expectedFavoritesList))
    }


}
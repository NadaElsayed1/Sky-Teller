package com.example.clearsky.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clearsky.model.CurrentResponseApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class WeatherLocalDataSourceTest{

    @get:Rule
    val rule = InstantTaskExecutorRule()
    lateinit var database: WeatherDatabase
    lateinit var localDataSource: WeatherLocalDataSource

    @Before
    fun setup(){
        //creation DB object
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).allowMainThreadQueries().build()

        //getting Dao using DB object
        localDataSource = WeatherLocalDataSource(database.weatherDao())
    }
    @After
    fun tearDown() = database.close()

    @Test
    fun getFavorites_retrievesListOfFavoriteCities() = runBlocking {
        // Given: Add two favorite cities to the database
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

        // Add the cities to the favorites
        localDataSource.addFavorite(favoriteCity1)
        localDataSource.addFavorite(favoriteCity2)

        // When: Retrieving the favorites list
        val result = localDataSource.getFavorites().first() // Collect the first emitted value

        // Then: Verify that the retrieved list matches the added favorite cities
        val expectedFavoritesList = listOf(favoriteCity1, favoriteCity2)
        assertThat(result, equalTo(expectedFavoritesList))
    }

    @Test
    fun addFavorite_addsCityToFavorites() = runBlocking {
        // Given: A favorite city to add
        val favoriteCity = CurrentResponseApi(
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

        // When: Adding the city to the favorites
        localDataSource.addFavorite(favoriteCity)

        // Then: Verify that the city was added to the favorites
        val result = localDataSource.getFavorites().first() // Collect the first emitted value
        val expectedFavoritesList = listOf(favoriteCity)
        assertThat(result, equalTo(expectedFavoritesList))
    }

    @Test
    fun removeFavorite_removesCityFromFavorites() = runBlocking {
        // Given: A city added to the favorites
        val favoriteCity = CurrentResponseApi(
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

        // Add city to favorites
        localDataSource.addFavorite(favoriteCity)

        // When: Removing the city from favorites
        localDataSource.removeFavorite(favoriteCity)

        // Then: Verify that the city was removed from the favorites
        val result = localDataSource.getFavorites().first() // Collect the first emitted value
        val expectedFavoritesList = emptyList<CurrentResponseApi>()
        assertThat(result, equalTo(expectedFavoritesList))
    }
}
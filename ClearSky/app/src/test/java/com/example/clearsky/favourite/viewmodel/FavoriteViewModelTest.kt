package com.example.clearsky.favourite.viewmodel

import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.clearsky.getOrAwaitValue
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.repository.FakeWeatherRepository
import com.example.clearsky.model.repository.IWeatherRepository
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class FavoriteViewModelTest {
    private lateinit var repository: IWeatherRepository
    private lateinit var viewModel: FavoriteViewModel

    @get:Rule
    val viewModelRule = ViewModelRule()

    @Before
    fun setup() {
        repository = FakeWeatherRepository()
        viewModel = FavoriteViewModel(repository)
    }

    @Test
    fun fetchFavorites_success_updatesLiveData() = runTest {
        // Given: Set up expected data
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

        (repository as FakeWeatherRepository).addFavorite(favoriteCity)

        // When: fetchFavorites is called
        viewModel.fetchFavorites()

        // Advance until all coroutines are complete
//        advanceUntilIdle()
        runCurrent()
        shadowOf(Looper.getMainLooper()).idle()


        // Then: Assert LiveData is updated
        val result = viewModel.favorites.getOrAwaitValue(time = 20)
        assertThat(result, `is`(listOf(favoriteCity)))
    }

    @Test
    fun addFavorite_success_updatesFavoritesList() = runTest {
        // Given: Set up expected data
        val newFavoriteCity = CurrentResponseApi(
            id = 2,
            name = "Alexandria",
            base = "stations",
            clouds = CurrentResponseApi.Clouds(all = 80),
            cod = 200,
            coord = CurrentResponseApi.Coord(lat = 31.2156, lon = 29.9553),
            dt = 1618317040,
            main = CurrentResponseApi.Main(
                feelsLike = 305.15,
                grndLevel = 1013,
                humidity = 45,
                pressure = 1016,
                seaLevel = 1013,
                temp = 300.15,
                tempMax = 302.15,
                tempMin = 298.15
            ),
            rain = null,
            sys = CurrentResponseApi.Sys(
                country = "EG",
                id = 2,
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

        // When: addFavorite is called
        viewModel.addFavorite(newFavoriteCity)
        runCurrent()
        shadowOf(Looper.getMainLooper()).idle()
        // Then: Assert the new favorite city is added to the list
        val result = viewModel.favorites.getOrAwaitValue(time = 20)
        assertThat(result.contains(newFavoriteCity), `is`(true))
    }

    @Test
    fun removeFavorite_success_updatesFavoritesList() = runTest {
        // Given: Set up data
        val cityToRemove = CurrentResponseApi(
            id = 3,
            name = "Giza",
            base = "stations",
            clouds = CurrentResponseApi.Clouds(all = 90),
            cod = 200,
            coord = CurrentResponseApi.Coord(lat = 30.0131, lon = 31.2089),
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
                id = 3,
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

        (repository as FakeWeatherRepository).addFavorite(cityToRemove)

        // When: removeFavorite is called
        viewModel.removeFavorite(cityToRemove)
        runCurrent()
        shadowOf(Looper.getMainLooper()).idle()
        // Then: Assert the city is removed from the favorites list
        val result = viewModel.favorites.getOrAwaitValue(time = 20)
        assertThat(result.contains(cityToRemove), `is`(false))
    }
}

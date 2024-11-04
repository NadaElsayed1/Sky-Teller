package com.example.clearsky.favourite.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clearsky.home.viewmodel.WeatherViewModel
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.repository.IWeatherRepository
import com.example.clearsky.model.repository.WeatherRepository
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repository: IWeatherRepository) : ViewModel() {
    private val _favorites = MutableLiveData<List<CurrentResponseApi>>()
    val favorites: LiveData<List<CurrentResponseApi>> get() = _favorites

    fun fetchFavorites() {
        viewModelScope.launch {
            val favoritesList = repository.getFavorites()
            _favorites.postValue(favoritesList)
        }
    }

    fun addFavorite(city: CurrentResponseApi) {
        viewModelScope.launch {
            try {
                repository.addFavorite(city)
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error adding favorite city: ${e.message}")
            }
        }
    }

    fun removeFavorite(city: CurrentResponseApi) {
        viewModelScope.launch {
            try {
                repository.removeFavorite(city)
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error removing favorite city: ${e.message}")
            }
        }
    }
}

class FavouriteViewModelFactory(
    private val repository: IWeatherRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            return FavoriteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

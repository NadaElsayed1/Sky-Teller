package com.example.clearsky.setting.viewmodel

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?> get() = _currentLocation

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        val latitude = sharedPreferences.getString("currentLocation", null)?.split(",")?.get(0)?.toDoubleOrNull()
        val longitude = sharedPreferences.getString("currentLocation", null)?.split(",")?.get(1)?.toDoubleOrNull()

        if (latitude != null && longitude != null) {
            _currentLocation.value = Location("").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
        }
    }

    fun saveCurrentLocation(location: Location) {
        _currentLocation.value = location
        sharedPreferences.edit().putString("currentLocation", "${location.latitude},${location.longitude}").apply()
    }
}

class SettingsViewModelFactory(
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

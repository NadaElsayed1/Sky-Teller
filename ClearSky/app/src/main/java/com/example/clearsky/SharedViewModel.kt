package com.example.clearsky

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _tempUnit = MutableLiveData<String>("Celsius")
    val tempUnit: LiveData<String> = _tempUnit

    private val _language = MutableLiveData<String>("Default")
    val language: LiveData<String> = _language

    private val _windSpeedUnit = MutableLiveData<String>("meter/sec")
    val windSpeedUnit: LiveData<String> = _windSpeedUnit

    private val _location = MutableLiveData<String>("GPS")
    val location: LiveData<String> = _location
    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> = _currentLocation

    fun setCurrentLocation(location: Location) {
        _currentLocation.value = location
    }

    fun setTempUnit(unit: String) {
        _tempUnit.value = unit
    }

    fun setLanguage(language: String) {
        _language.value = language
    }

    fun setWindSpeedUnit(unit: String) {
        _windSpeedUnit.value = unit
    }

    fun setLocation(location: String) {
        _location.value = location
    }
}

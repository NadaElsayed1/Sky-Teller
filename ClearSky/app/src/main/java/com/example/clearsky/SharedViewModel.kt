//package com.example.clearsky
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//
//class SharedViewModel : ViewModel() {
//    private val _location = MutableLiveData<Pair<Double, Double>>()
//    val location: LiveData<Pair<Double, Double>> get() = _location
//
//    fun setLocation(lat: Double, lon: Double) {
//        _location.value = Pair(lat, lon)
//    }
//}

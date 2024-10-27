package com.example.clearsky.favourite.viewmodel

import androidx.lifecycle.ViewModel
import com.example.clearsky.network.ApiClient
import com.example.clearsky.network.ApiService
import com.example.clearsky.model.repository.CityRepository

class CityViewModel(val repository: CityRepository): ViewModel()
{
    constructor():this(CityRepository(ApiClient().getClient().create(ApiService::class.java)))

    fun loadCity(q: String, limit: Int)= repository.getCities(q, limit)
}
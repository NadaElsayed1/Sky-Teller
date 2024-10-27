package com.example.clearsky.model.repository

import com.example.clearsky.network.ApiService

class CityRepository(val api: ApiService) {

    fun getCities(q:String, limit:Int)= api.getCitiesList(q,limit,"9832068f1c229dbbef08a89208bb2d8f")
}
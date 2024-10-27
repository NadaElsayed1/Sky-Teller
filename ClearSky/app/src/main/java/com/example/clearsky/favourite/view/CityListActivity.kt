package com.example.clearsky.favourite.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.clearsky.databinding.ActivityCityListBinding

class CityListActivity : AppCompatActivity() {
    lateinit var binding: ActivityCityListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
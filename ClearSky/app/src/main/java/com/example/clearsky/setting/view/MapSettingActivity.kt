package com.example.clearsky.setting.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.clearsky.R
import com.example.clearsky.databinding.ActivityMapSettingBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.Locale

class MapSettingActivity : AppCompatActivity() {
        private lateinit var binding: ActivityMapSettingBinding
        private lateinit var mapView: MapView
        private var selectedLocation: GeoPoint? = null
        private lateinit var marker: Marker
        private lateinit var fusedLocationClient: FusedLocationProviderClient

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
            binding = ActivityMapSettingBinding.inflate(layoutInflater)
            setContentView(binding.root)

            mapView = binding.map
            mapView.setMultiTouchControls(true)

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                getCurrentLocation()
            }

            mapView.overlays.add(object : Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    val geoPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt())
                    selectedLocation = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                    marker.position = selectedLocation
                    marker.title = "Selected Location"
                    mapView.invalidate()
                    return true
                }
            })

            binding.buttonSelect.setOnClickListener {
                selectedLocation?.let { geoPoint ->
                    val cityName = getCityName(geoPoint.latitude, geoPoint.longitude)
                    val resultIntent = Intent().apply {
                        putExtra("lat", geoPoint.latitude)
                        putExtra("lon", geoPoint.longitude)
                        putExtra("name", cityName)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }?: run {
                    Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun getCityName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses?.isNotEmpty() == true) {
            addresses[0]?.locality ?: "Unknown City"
        } else {
            "Unknown City"
        }}
            private fun getCurrentLocation() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLocation = GeoPoint(location.latitude, location.longitude)
                        setupMap(currentLocation)
                    } else {
                        val defaultLocation = GeoPoint(30.0444, 31.2357)
                        setupMap(defaultLocation)
                    }
                }
            }
        }

        private fun setupMap(location: GeoPoint) {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(location)

            marker = Marker(mapView)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.icon = resources.getDrawable(R.drawable.map_marker)
            marker.position = location
            marker.title = "Current Location"
            mapView.overlays.add(marker)
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getCurrentLocation()
                } else {
                    Log.i("TAG", "onRequestPermissionsResult: permission denied")
                }
            }
        }

        companion object {
            private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        }
    }
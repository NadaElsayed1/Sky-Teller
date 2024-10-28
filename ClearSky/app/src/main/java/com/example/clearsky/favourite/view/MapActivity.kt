package com.example.clearsky.favourite.view
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.clearsky.R
import com.example.clearsky.databinding.ActivityMapBinding
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapView: MapView
    private var selectedLocation: GeoPoint? = null
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.map
        mapView.setMultiTouchControls(true)
        val defaultLocation = GeoPoint(30.0444, 31.2357) // Cairo
        mapView.controller.setZoom(10.0)
        mapView.controller.setCenter(defaultLocation)

        marker = Marker(mapView)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = resources.getDrawable(R.drawable.map_marker) // Set marker icon if available
        mapView.overlays.add(marker)

        // Use Overlay for click listener on the map
        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val geoPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt())
                selectedLocation = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                marker.position = selectedLocation
                marker.title = "Selected Location"
                mapView.invalidate() // Refresh the map view
                return true
            }
        })

        binding.buttonSelect.setOnClickListener {
            selectedLocation?.let { geoPoint ->
                val resultIntent = Intent().apply {
                    putExtra("lat", geoPoint.latitude)
                    putExtra("lon", geoPoint.longitude)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}


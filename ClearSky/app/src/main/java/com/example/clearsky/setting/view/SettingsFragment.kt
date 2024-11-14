package com.example.clearsky.setting.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.clearsky.R
import com.example.clearsky.SharedViewModel
import com.example.clearsky.databinding.FragmentSettingsBinding
import com.example.clearsky.home.view.HomeFragment
import com.example.clearsky.setting.viewmodel.SettingsViewModel
import com.example.clearsky.setting.viewmodel.SettingsViewModelFactory
import java.util.Locale


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationManager: LocationManager
    //android KTX(to write the code at concise way) SettingsViewModel by viewModels{factory instance}
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModelFactory(sharedPreferences) }

    private val MAP_REQUEST_CODE = 2000
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

        settingsViewModel.currentLocation.observe(viewLifecycleOwner, Observer { location ->
            location?.let {
                //android KTX(to write the code at concise way)
                //sharedPreferences.edit().putString("currentLocation", "${it.latitude},${it.longitude}").apply()
                sharedPreferences.edit { putString("currentLocation", "${it.latitude},${it.longitude}") }
            }
        })

        loadPreferences()
        setupListeners()
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return binding.root
    }

    private fun setupListeners() {
        binding.languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val language = when (checkedId) {
                R.id.rbArabic -> "ar"
                R.id.rbEnglish -> "en"
                else -> "en"
            }
            sharedPreferences.edit { putString("language", language) }
            setLocale(language)
        }

        binding.tempUnitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val tempUnit = when (checkedId) {
                R.id.rbCelsius -> "Celsius"
                R.id.rbKelvin -> "Kelvin"
                else -> "Fahrenheit"
            }
            sharedPreferences.edit().putString("tempUnit", tempUnit).apply()
        }

        binding.locationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbGPS -> {
                    sharedPreferences.edit(){putString("location", "GPS")}
                    requestCurrentLocation()
                }
                R.id.rbMap -> {
                    sharedPreferences.edit(){putString("location", "Map")}
                    openMap()
                }
            }
        }

        binding.windSpeedUnitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val windSpeedUnit = when (checkedId) {
                R.id.rbMeterSec -> "meter/sec"
                else -> "mile/hour"
            }
            sharedPreferences.edit().putString("windSpeedUnit", windSpeedUnit).apply()
        }
    }

    private fun openMap() {
        val intent = Intent(requireContext(), MapSettingActivity::class.java)
        startActivityForResult(intent, MAP_REQUEST_CODE)
    }

    private fun requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateToHome() {
        val homeFragment = HomeFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, homeFragment)
            .addToBackStack(null)
            .commit()
    }
    private fun getCurrentLocation() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showEnableGPSDialog()
            return
        }

        val location: Location? = if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else {
            null
        }
        location?.let {
            updateLocationData(it)
        } ?: run {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, locationListener)
        }
    }
    private fun updateLocationData(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val cityName = addresses?.firstOrNull()?.locality ?: "Unknown"
        sharedPreferences.edit {
            putFloat("lat", location.latitude.toFloat())
            putFloat("lon", location.longitude.toFloat())
            putString("name", cityName)
        }
        navigateToHome()
    }

    private fun showEnableGPSDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enable GPS")
        builder.setMessage("GPS is disabled. Please enable GPS for better location services.")
        builder.setPositiveButton("Settings") { dialog, _ ->
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            settingsViewModel.saveCurrentLocation(location)
            locationManager.removeUpdates(this)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun setLocale(localeName: String) {
        val locale = when (localeName) {
            "ar" -> Locale("ar")
            "en" -> Locale("en")
            else -> Locale.getDefault()
        }
        Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        requireActivity().recreate()
    }

    private fun loadPreferences() {
        val language = sharedPreferences.getString("language", "en")
        binding.rbArabic.isChecked = language == "ar"
        binding.rbEnglish.isChecked = language == "en"

        val tempUnit = sharedPreferences.getString("tempUnit", "Celsius")
        binding.rbCelsius.isChecked = tempUnit == "Celsius"
        binding.rbKelvin.isChecked = tempUnit == "Kelvin"
        binding.rbFahrenheit.isChecked = tempUnit == "Fahrenheit"

        val location = sharedPreferences.getString("location", "GPS")
        binding.rbGPS.isChecked = location == "GPS"
        binding.rbMap.isChecked = location == "Map"

        val windSpeedUnit = sharedPreferences.getString("windSpeedUnit", "meter/sec")
        binding.rbMeterSec.isChecked = windSpeedUnit == "meter/sec"
        binding.rbMileHour.isChecked = windSpeedUnit == "mile/hour"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra("lon", 0.0) ?: 0.0
            val cityName = data?.getStringExtra("name") ?: "Unknown"

            sharedPreferences.edit {
                putFloat("lat", latitude.toFloat())
                putFloat("lon", longitude.toFloat())
                putString("name", cityName)
            }

            navigateToHome()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
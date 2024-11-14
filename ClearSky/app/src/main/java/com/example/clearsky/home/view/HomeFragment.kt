package com.example.clearsky.home.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.util.Calendar
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.clearsky.R
import com.example.clearsky.SharedViewModel
import com.example.clearsky.databinding.FragmentHomeBinding
import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.home.viewmodel.WeatherViewModel
import com.example.clearsky.home.viewmodel.WeatherViewModelFactory
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.model.repository.WeatherRepository
import com.example.clearsky.network.RemoteDataStructure
import com.example.clearsky.setting.view.SettingsFragment.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.github.matteobattilana.weather.PrecipType
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    private val weatherViewModel: WeatherViewModel by lazy {
        val repository = WeatherRepository(RemoteDataStructure, WeatherLocalDataSource.getInstance(requireContext()))
        val factory = WeatherViewModelFactory(repository)
        ViewModelProvider(this, factory)[WeatherViewModel::class.java]
    }

    private val calendar by lazy { Calendar.getInstance() }
    private val forecastHoursAdapter by lazy { ForecastHoursAdapter() }
    private val forecastDaysAdapter by lazy { ForecastDaysAdapter() }

    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        when (key) {
            "tempUnit" -> updateTemperatureUnit(sharedPrefs.getString(key, "Celsius") ?: "Celsius")
            "language" -> updateLanguage(sharedPrefs.getString(key, "en") ?: "en")
            "windSpeedUnit" -> updateWindSpeedUnit(sharedPrefs.getString(key, "meter/sec") ?: "meter/sec")
            "location" -> {
                val locationSetting = sharedPrefs.getString("location", "GPS")
                if (locationSetting == "GPS") {
//                    requestGpsLocationUpdate()
                    initializeUI()
                } else {
                    val lat = sharedPrefs.getFloat("lat", 30.0444f).toDouble()
                    val lon = sharedPrefs.getFloat("lon", 31.2357f).toDouble()
                    val cityName = sharedPrefs.getString("name", "Cairo")
                    binding.cityTxt.text = cityName
                    observeWeatherData(lat, lon)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
        if (isFirstLaunch) {
            with(sharedPreferences.edit()) {
                putFloat("lat", 30.0444f)
                putFloat("lon", 31.2357f)
                putString("name", "Cairo")
                putBoolean("isFirstLaunch", false)
                apply()
            }
        }
        setupPreferences()
        return binding.root
    }

    private fun setupPreferences() {
        applyPreferences()
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    private fun applyPreferences() {
        val tempUnit = sharedPreferences.getString("tempUnit", "Celsius") ?: "Celsius"
        val language = sharedPreferences.getString("language", "en") ?: "en"
        val windSpeedUnit = sharedPreferences.getString("windSpeedUnit", "meter/sec") ?: "meter/sec"
        updateTemperatureUnit(tempUnit)
        updateLanguage(language)
        updateWindSpeedUnit(windSpeedUnit)
    }

    private fun updateTemperatureUnit(unit: String) {
        weatherViewModel.currentWeatherData.value?.let { data ->
            binding.currentTempTxt.text = convertTemperature(data.main?.temp ?: 0.0, unit)
            binding.feelsLikeTxt.text = convertTemperature(data.main?.feelsLike ?: 0.0, unit)
        } ?: run {
//            binding.currentTempTxt.text = getString(R.string.data_unavailable)
//            binding.feelsLikeTxt.text = getString(R.string.data_unavailable)
        }
    }

    private fun updateLanguage(language: String) {
        Locale.setDefault(Locale(language))
    }

    private fun updateWindSpeedUnit(unit: String) {
        val currentWindSpeed = 5.0
        binding.windTxt.text = convertWindSpeed(currentWindSpeed, unit)
    }

    private fun requestGpsLocationUpdate() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let {
                observeWeatherData(it.latitude, it.longitude)
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStatusBar()
        initializeUI()
        observeWeatherDataFromPreferences()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra("lat", 0.0) ?: return
            val longitude = data?.getDoubleExtra("lon", 0.0) ?: return

            sharedPreferences.edit {
                putFloat("lat", latitude.toFloat())
                putFloat("lon", longitude.toFloat())
                putString("name", "Unknown")
            }

            observeWeatherData(latitude, longitude)
        }
    }
    private fun setupStatusBar() {
        activity?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = true
        }
    }

    private fun initializeUI() {
        binding.apply {
            val lat = sharedPreferences.getFloat("lat", 30.0444f).toDouble()
            val lon = sharedPreferences.getFloat("lon", 31.2357f).toDouble()
            val cityName = sharedPreferences.getString("name", "Cairo") ?: "Cairo"
            cityTxt.text = cityName
            setupForecastRecyclerViews()
            observeWeatherData(lat, lon)
        }
    }
    private fun observeWeatherData(lat: Double, lon: Double) {
        val tempUnit = sharedPreferences.getString("tempUnit", "Celsius") ?: "Celsius"
        val language = sharedPreferences.getString("language", "en") ?: "en"

        weatherViewModel.fetchCurrentWeather(lat, lon, if (tempUnit == "Celsius") "metric" else "imperial", language)
        weatherViewModel.fetchForecastWeather(lat, lon, if (tempUnit == "Celsius") "metric" else "imperial", language)

        weatherViewModel.currentWeatherData.observe(viewLifecycleOwner) { data ->
            data?.let {
                binding.progressBar.visibility = View.GONE
                binding.detailLayout.visibility = View.VISIBLE
                updateWeatherUI(it)
            }
        }

        weatherViewModel.forecastWeatherData.observe(viewLifecycleOwner) { forecastList ->
            binding.blurView.visibility = View.VISIBLE
            forecastHoursAdapter.differ.submitList(forecastList)
            forecastDaysAdapter.differ.submitList(getUniqueDaysForecast(forecastList))
        }
    }

    private fun observeWeatherDataFromPreferences() {
        val lat = sharedPreferences.getFloat("lat", 30.0444f).toDouble()
        val lon = sharedPreferences.getFloat("lon", 31.2357f).toDouble()
        observeWeatherData(lat, lon)
    }

    private fun updateWeatherUI(data: CurrentResponseApi) {
        val tempUnit = sharedPreferences.getString("tempUnit", "Celsius") ?: "Celsius"
        val windSpeedUnit = sharedPreferences.getString("windSpeedUnit", "meter/sec") ?: "meter/sec"

        binding.apply {
            statusTxt.text = data.weather?.get(0)?.main ?: "-"
            windTxt.text = convertWindSpeed(data.wind?.speed ?: 0.0, windSpeedUnit)
            humidityTxt.text = "${data.main?.humidity}%"
            currentTempTxt.text = convertTemperature(data.main?.temp ?: 0.0, tempUnit)
            maxTempTxt.text = convertTemperature(data.main?.tempMax ?: 0.0, tempUnit)
            minTempTxt.text = convertTemperature(data.main?.tempMin ?: 0.0, tempUnit)
            feelsLikevalue.text = convertTemperature(data.main?.feelsLike ?: 0.0, tempUnit)
            pressurevalue.text = String.format("${data.main?.pressure ?: 0} %s", getString(R.string.unit_pressure))

//            Glide.with(requireContext())
//                .load(data.weather?.get(0)?.icon)
//                .placeholder(R.drawable.arrow_up)
//                .into(imageView5)
//
//            Glide.with(requireContext())
//                .load(data.weather?.get(0)?.icon)
//                .placeholder(R.drawable.arrow_down)
//                .into(imageView6)

            val backgroundRes = if (isNightNow()) R.drawable.night_bg else setDynamicallyWallpaper(data.weather?.get(0)?.icon ?: "-")
            bgImage.setImageResource(backgroundRes)
            setEffectRainSnow(data.weather?.get(0)?.icon ?: "-")
        }
    }

    private fun setupForecastRecyclerViews() {
        binding.rvForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = forecastHoursAdapter
        }

        binding.fiveDayForecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = forecastDaysAdapter
        }
    }

    //need update
    private fun convertTemperature(celsiusTemp: Double, unit: String): String {
        return when (unit) {
            "Celsius" -> String.format("%.1f %s", celsiusTemp, getString(R.string.unit_celsius))
            "Fahrenheit" -> String.format("%.1f %s", (celsiusTemp * 9 / 5) + 32, getString(R.string.unit_fahrenheit))
            else -> String.format("%.1f %s", celsiusTemp + 273.15, getString(R.string.unit_kelvin))
        }
    }

    private fun convertWindSpeed(speed: Double, unit: String): String {
        return when (unit) {
            "mile/hour" -> String.format("%.1f %s", speed * 2.23694, getString(R.string.unit_mile_per_hour))
            else -> String.format("%.1f %s", speed, getString(R.string.unit_meter_per_sec))
        }
    }

    private fun getUniqueDaysForecast(forecastList: List<ForecastResponseApi.Forecast>): List<ForecastResponseApi.Forecast> {
        val uniqueDaysMap = mutableMapOf<String, ForecastResponseApi.Forecast>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        forecastList.forEach { forecast ->
            val date = dateFormat.format(dateTimeFormat.parse(forecast.dtTxt))
            uniqueDaysMap[date] = forecast
        }

        return uniqueDaysMap.values.take(5)
    }

    private fun isNightNow(): Boolean = calendar.get(Calendar.HOUR_OF_DAY) >= 18

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01", "02", "03", "04" -> R.drawable.haze_bg
             "13", "50" -> R.drawable.night_bg
            "09", "10" -> R.drawable.rainy_bg
            "11" -> R.drawable.haze_bg
            else -> R.drawable.night_bg
        }
    }

    private fun setEffectRainSnow(icon: String) {
        when (icon.dropLast(1)) {
            "09", "10" -> binding.weatherView.setWeatherData(PrecipType.RAIN)
            "13" -> binding.weatherView.setWeatherData(PrecipType.SNOW)
            else -> binding.weatherView.setWeatherData(PrecipType.CLEAR)
        }
    }

    override fun onDestroyView() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val MAP_REQUEST_CODE = 1
    }
}
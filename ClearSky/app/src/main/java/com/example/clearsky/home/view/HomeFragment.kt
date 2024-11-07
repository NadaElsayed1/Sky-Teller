package com.example.clearsky.home.view

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.clearsky.R
import com.example.clearsky.databinding.FragmentHomeBinding
import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.home.viewmodel.WeatherViewModel
import com.example.clearsky.home.viewmodel.WeatherViewModelFactory
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.model.repository.WeatherRepository
import com.example.clearsky.network.RemoteDataStructure
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
    companion object {
        private const val MAP_REQUEST_CODE = 100
    }

    private val calendar by lazy { Calendar.getInstance() }
    private val forecastHoursAdapter by lazy { ForecastHoursAdapter() }
    private val forecastDaysAdapter by lazy { ForecastDaysAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val currentLocation = sharedPreferences.getString("currentLocation", null)
        currentLocation?.let {
            val latLong = it.split(",")
            val latitude = latLong[0].toDouble()
            val longitude = latLong[1].toDouble()}
        applyPreferences()
        setupSharedPreferencesListener()
        return binding.root
    }

    private fun applyPreferences() {
        val tempUnit = sharedPreferences.getString("tempUnit", "Celsius") ?: "Celsius"
        val language = sharedPreferences.getString("language", "Default") ?: "Default"
        val windSpeedUnit = sharedPreferences.getString("windSpeedUnit", "meter/sec") ?: "meter/sec"

        updateTemperatureUnit(tempUnit)
        updateLanguage(language)
        updateWindSpeedUnit(windSpeedUnit)
    }

    private fun updateTemperatureUnit(unit: String) {
        weatherViewModel.currentWeatherData.value?.let { data ->
            binding.currentTempTxt.text = convertTemperature(data.main?.temp ?: 0.0, unit)
            binding.feelsLikeTxt.text = convertTemperature(data.main?.feelsLike ?: 0.0, unit)  // Feels like temperature
        } ?: run {
            binding.currentTempTxt.text = "Data not available"
            binding.feelsLikeTxt.text = "Data not available"
        }
    }

    private fun updateLanguage(language: String) {
        Locale.setDefault(Locale(language))
    }

    private fun updateWindSpeedUnit(unit: String) {
        val currentWindSpeed = 5.0
        binding.windTxt.text = convertWindSpeed(currentWindSpeed, unit)
    }


    private fun setupSharedPreferencesListener() {
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "tempUnit" -> updateTemperatureUnit(sharedPrefs.getString(key, "Celsius") ?: "Celsius")
                "language" -> updateLanguage(sharedPrefs.getString(key, "Default") ?: "Default")
                "windSpeedUnit" -> updateWindSpeedUnit(sharedPrefs.getString(key, "meter/sec") ?: "meter/sec")
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = true
        }

        binding.apply {
            val lat = sharedPreferences.getFloat("lat", 30.0444f)
            val lon = sharedPreferences.getFloat("lon", 31.2357f)
            val name = sharedPreferences.getString("name", "Cairo") ?: "Cairo"

            weatherViewModel.fetchCurrentWeather(lat.toDouble(), lon.toDouble(), "metric", "en")
            cityTxt.text = name
            weatherViewModel.fetchForecastWeather(lat.toDouble(), lon.toDouble(), "metric", "en")

            progressBar.visibility = View.VISIBLE
            imageView5.visibility = View.VISIBLE
            imageView6.visibility = View.VISIBLE

            val tempUnit = sharedPreferences.getString("tempUnit", "Celsius") ?: "Celsius"
            val windSpeedUnit = sharedPreferences.getString("windSpeedUnit", "meter/sec") ?: "meter/sec"
            val language = sharedPreferences.getString("language", "en") ?: "en"

            weatherViewModel.currentWeatherData.observe(viewLifecycleOwner) { data ->
                progressBar.visibility = View.GONE
                detailLayout.visibility = View.VISIBLE
                data?.let {
                    statusTxt.text = it.weather?.get(0)?.main ?: "-"
                    windTxt.text = convertWindSpeed(it.wind?.speed ?: 0.0, windSpeedUnit)
                    humidityTxt.text = "${it.main?.humidity}%"
                    currentTempTxt.text = convertTemperature(it.main?.temp ?: 0.0, tempUnit)
                    maxTempTxt.text = convertTemperature(it.main?.tempMax ?: 0.0, tempUnit)
                    minTempTxt.text = convertTemperature(it.main?.tempMin ?: 0.0, tempUnit)
                    feelsLikeTxt.text = getString(R.string.feelslike)
                    feelsLikevalue.text = convertTemperature(it.main?.feelsLike ?: 0.0, tempUnit)
                    pressureTxt.text = getString(R.string.pressure)
                    pressurevalue.text = "${it.main?.pressure ?: 0} hPa"

                    Glide.with(requireContext())
                        .load(data.weather?.get(0)?.icon)
                        .placeholder(R.drawable.arrow_up)
                        .into(imageView5)

                    Glide.with(requireContext())
                        .load(data.weather?.get(0)?.icon)
                        .placeholder(R.drawable.arrow_down)
                        .into(imageView6)

                    val drawable = if (isNightNow()) R.drawable.night_bg else {
                        setDynamicallyWallpaper(it.weather?.get(0)?.icon ?: "-")
                    }
                    bgImage.setImageResource(drawable)
                    setEffectRainSnow(it.weather?.get(0)?.icon ?: "-")
                }
            }

            weatherViewModel.forecastWeatherData.observe(viewLifecycleOwner) { forecastList ->
                blurView.visibility = View.VISIBLE
                binding.rvForecast.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    adapter = forecastHoursAdapter
                }
                forecastHoursAdapter.differ.submitList(forecastList)
            }

            weatherViewModel.forecastWeatherData.observe(viewLifecycleOwner) { forecastList ->
                val uniqueDaysForecast = getUniqueDaysForecast(forecastList)

                binding.fiveDayForecastRecyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = forecastDaysAdapter
                }
                forecastDaysAdapter.differ.submitList(uniqueDaysForecast)
            }

            weatherViewModel.fetchCurrentWeather(lat.toDouble(), lon.toDouble(), if (tempUnit == "Celsius") "metric" else "imperial", language)
            weatherViewModel.fetchForecastWeather(lat.toDouble(), lon.toDouble(), if (tempUnit == "Celsius") "metric" else "imperial", language)
        }
    }
    private fun convertTemperature(kelvinTemp: Double, unit: String): String {
        return when (unit) {
            "Celsius" -> String.format("%.1f°C", kelvinTemp)
            "Fahrenheit" -> String.format("%.1f°F", (kelvinTemp) * 9/5 + 32)
            else -> String.format("%.1fK", kelvinTemp + 273.15)
        }
    }

    private fun convertWindSpeed(speed: Double, unit: String): String {
        return when (unit) {
            "mile/hour" -> String.format("%.1f m/h", speed * 2.23694)
            else -> String.format("%.1f m/s", speed)
        }
    }

    private fun getUniqueDaysForecast(forecastList: List<ForecastResponseApi.Forecast>): List<ForecastResponseApi.Forecast> {
        val uniqueDaysMap = mutableMapOf<String, ForecastResponseApi.Forecast>()
        for (forecast in forecastList) {
            val date = SimpleDateFormat("yyyy-MM-dd").format(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(forecast.dtTxt)
            )
            if (!uniqueDaysMap.containsKey(date)) {
                uniqueDaysMap[date] = forecast
            }
        }
        return uniqueDaysMap.values.take(5)
    }

    private fun isNightNow(): Boolean = calendar.get(Calendar.HOUR_OF_DAY) >= 18

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01", "02", "03", "04", "13" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.cloudy_bg
            }
            "09", "10", "11" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.rainy_bg
            }
            "50" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.haze_bg
            }
            else -> 0
        }
    }

    private fun setEffectRainSnow(icon: String) {
        when (icon.dropLast(1)) {
            "09", "10", "11" -> initWeatherView(PrecipType.RAIN)
            "13" -> initWeatherView(PrecipType.SNOW)
            else -> initWeatherView(PrecipType.CLEAR)
        }
    }

    private fun initWeatherView(type: PrecipType) {
        binding.weatherView.apply {
            setWeatherData(type)
            angle = -20
            emissionRate = 100.0f
            fadeOutPercent = 0.9f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener { _, _ -> }
        _binding = null
    }
}
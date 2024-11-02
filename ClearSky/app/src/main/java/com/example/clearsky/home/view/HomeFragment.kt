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
//    private lateinit var sharedViewModel: SharedViewModel

    private val weatherViewModel: WeatherViewModel by lazy {
        // Initialize the repository with both remote and local data sources
        val repository = WeatherRepository(RemoteDataStructure, WeatherLocalDataSource.getInstance(requireContext()))
        // Create ViewModelFactory with the repository
        val factory = WeatherViewModelFactory(repository)
        // Get the WeatherViewModel using the factory
        ViewModelProvider(this, factory)[WeatherViewModel::class.java]
    }

    private val calendar by lazy { Calendar.getInstance() }
    private val forecastHoursAdapter by lazy { ForecastHoursAdapter() }
    private val forecastDaysAdapter by lazy { ForecastDaysAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        applyPreferences()
        setupSharedPreferencesListener()
        return binding.root    }
    private fun applyPreferences() {
        /*set init state*/
        val tempUnit = sharedPreferences.getString("tempUnit", "Celsius") ?: "Celsius"
        val language = sharedPreferences.getString("language", "Default") ?: "Default"
        val windSpeedUnit = sharedPreferences.getString("windSpeedUnit", "meter/sec") ?: "meter/sec"

        // Update ui
        updateTemperatureUnit(tempUnit)
        updateLanguage(language)
        updateWindSpeedUnit(windSpeedUnit)
    }

    private fun updateTemperatureUnit(unit: String) {
        val currentTemperature = 22.0
        binding.currentTempTxt.text = convertTemperature(currentTemperature, unit)

//        when (unit) {
//            "Celsius" -> {
//                val currentTemperatureInCelsius = currentTemperatureInKelvin - 273.15
//                binding.currentTempTxt.text = String.format("%.1f °C", currentTemperatureInCelsius)
//            }
//            "Kelvin" -> {
//                binding.currentTempTxt.text = String.format("%.1f K", currentTemperatureInKelvin)
//            }
//            "Fahrenheit" -> {
//                val currentTemperatureInFahrenheit = ((currentTemperatureInKelvin -273.15 )* 9/5) + 32
//                binding.currentTempTxt.text = String.format("%.1f °F", currentTemperatureInFahrenheit)
//            }
//        }
    }

    private fun updateLanguage(language: String) {
        when (language) {
            "Arabic" -> {
                Locale.setDefault(Locale("ar"))
            }
            "English" -> {
                Locale.setDefault(Locale("en"))
            }
            else -> {
                Locale.setDefault(Locale.getDefault())
            }
        }
        updateUIWithCurrentLanguage()
    }

    private fun updateWindSpeedUnit(unit: String) {
        val currentWindSpeed = 5.0
        binding.windTxt.text = convertWindSpeed(currentWindSpeed,unit)
//        when (unit) {
//            "meter/sec" -> {
//                binding.windTxt.text = String.format("%.1f m/s", currentWindSpeed)
//            }
//            "mile/hour" -> {
//                val windSpeedInMph = currentWindSpeed * 2.23694
//                binding.windTxt.text = String.format("%.1f mph", windSpeedInMph)
//            }
//        }
    }
    private fun updateUIWithCurrentLanguage() {
        // Implement logic to refresh any static UI text to apply the language changes
    }

    private fun setupSharedPreferencesListener() {
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "tempUnit" -> {
                    val tempUnit = sharedPrefs.getString(key, "Celsius") ?: "Celsius"
                    updateTemperatureUnit(tempUnit)
                }
                "language" -> {
                    val language = sharedPrefs.getString(key, "Default") ?: "Default"
                    updateLanguage(language)
                }
                "windSpeedUnit" -> {
                    val windSpeedUnit = sharedPrefs.getString(key, "meter/sec") ?: "meter/sec"
                    updateWindSpeedUnit(windSpeedUnit)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        activity?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = true
        }

        binding.apply {
            val lat = 30.0444
            val lon = 31.2357
            val name = "Cairo"


            // Use the coordinates as needed
            weatherViewModel.fetchCurrentWeather(lat, lon, "metric", "en")
            cityTxt.text = name
            progressBar.visibility = View.VISIBLE
            imageView5.visibility = View.VISIBLE
            imageView6.visibility = View.VISIBLE

            // Retrieve the user's temperature and wind speed unit preferences
            val tempUnit = sharedPreferences.getString("tempUnit", "Celsius") ?: "Celsius"
            val windSpeedUnit = sharedPreferences.getString("windSpeedUnit", "meter/sec") ?: "meter/sec"
            val language = sharedPreferences.getString("language", "en") ?: "en"

            // Observing current weather data
            weatherViewModel.currentWeatherData.observe(viewLifecycleOwner) { data ->
                progressBar.visibility = View.GONE
                detailLayout.visibility = View.VISIBLE
                data?.let {
                    statusTxt.text = it.weather?.get(0)?.main ?: "-"
                    windTxt.text = convertWindSpeed(it.wind?.speed ?: 0.0, windSpeedUnit)
                    humidityTxt.text = it.main?.humidity?.toString() + "%"
                    currentTempTxt.text = convertTemperature(it.main?.temp ?: 0.0, tempUnit)
                    maxTempTxt.text = convertTemperature(it.main?.tempMax ?: 0.0, tempUnit)
                    minTempTxt.text = convertTemperature(it.main?.tempMin ?: 0.0, tempUnit)

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

            // Observing forecast data for each 3 hours
            weatherViewModel.forecastWeatherData.observe(viewLifecycleOwner) { forecastList ->
                blurView.visibility = View.VISIBLE
                binding.rvForecast.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    adapter = forecastHoursAdapter
                }
                forecastHoursAdapter.differ.submitList(forecastList)
            }

            // Observing forecast data for 5 days
            weatherViewModel.forecastWeatherData.observe(viewLifecycleOwner) { forecastList ->
                val uniqueDaysForecast = getUniqueDaysForecast(forecastList)

                binding.fiveDayForecastRecyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = forecastDaysAdapter
                }
                forecastDaysAdapter.differ.submitList(uniqueDaysForecast)
            }

            // Trigger data loading with preference-based units
            weatherViewModel.fetchCurrentWeather(lat, lon, if (tempUnit == "Celsius") "metric" else "imperial", language)
            weatherViewModel.fetchForecastWeather(lat, lon, if (tempUnit == "Celsius") "metric" else "imperial", language)
//            sharedViewModel.location.observe(viewLifecycleOwner) { location ->
//                val (lat, lon) = location
//                weatherViewModel.fetchCurrentWeather(lat, lon, if (tempUnit == "Celsius") "metric" else "imperial", language)
//                weatherViewModel.fetchForecastWeather(lat, lon, if (tempUnit == "Celsius") "metric" else "imperial", language)
////                weatherViewModel.fetchCurrentWeather(lat, lon, "metric", "en")
//            }
        }
    }

    private fun convertTemperature(kelvinTemp: Double, unit: String): String {
        return when (unit) {
            "Celsius" -> String.format("%.1f", kelvinTemp - 273.15)
            "Fahrenheit" -> String.format("%.1f", (kelvinTemp - 273.15) * 9/5 + 32)
            else -> String.format("%.1f K", kelvinTemp)
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
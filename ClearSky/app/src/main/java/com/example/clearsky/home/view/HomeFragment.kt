package com.example.clearsky.home.view

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.R
import com.example.clearsky.databinding.FragmentHomeBinding
import com.example.clearsky.home.viewmodel.WeatherViewModel
import com.github.matteobattilana.weather.PrecipType
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val weatherViewModel: WeatherViewModel by lazy {
        ViewModelProvider(requireActivity()).get(WeatherViewModel::class.java)
    }
    private val calendar by lazy { Calendar.getInstance() }
    private val forecastHoursAdapter by lazy { ForecastHoursAdapter() }
    private val forecastDaysAdapter by lazy { ForecastDaysAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = true
        }

        binding.apply {
            val lat = 30.0444
            val lon = 31.2357
            val name = "Cairo"

            cityTxt.text = name
            progressBar.visibility = View.VISIBLE
            imageView5.visibility = View.VISIBLE
            imageView6.visibility = View.VISIBLE

            weatherViewModel.loadCurrentWeather(lat, lon, "metric").enqueue(object : retrofit2.Callback<CurrentResponseApi> {
                override fun onResponse(call: Call<CurrentResponseApi>, response: Response<CurrentResponseApi>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        progressBar.visibility = View.GONE
                        detailLayout.visibility = View.VISIBLE
                        data?.let {
                            statusTxt.text = it.weather?.get(0)?.main ?: "-"
                            windTxt.text = it.wind?.speed?.let { Math.round(it).toString() } + "Km"
                            humidityTxt.text = it.main?.humidity?.toString() + "%"
                            val kelvinTemp = it.main?.temp ?: 0.0
                            val celsiusTemp = kelvinTemp - 273.15
                            currentTempTxt.text = it.main?.temp?.let { Math.round(celsiusTemp).toString() } + "°"
                            maxTempTxt.text = Math.round(it.main?.tempMax?.let { it - 273.15 } ?: 0.0).toString() + "°"
                            minTempTxt.text = Math.round(it.main?.tempMin?.let { it - 273.15 } ?: 0.0).toString() + "°"

                            val drawable = if (isNightNow()) R.drawable.night_bg else {
                                setDynamicallyWallpaper(it.weather?.get(0)?.icon ?: "-")
                            }
                            bgImage.setImageResource(drawable)
                            setEffectRainSnow(it.weather?.get(0)?.icon ?: "-")
                        }
                    }
                }

                override fun onFailure(call: Call<CurrentResponseApi>, t: Throwable) {
                    Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_SHORT).show()
                }
            })

            val radius = 10f
            val decorView = activity?.window?.decorView
            val rootView = decorView?.findViewById<ViewGroup>(android.R.id.content)
            val windowBackground = decorView?.background

            rootView?.let {
                blurView.setupWith(it, RenderScriptBlur(requireContext()))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blurView.clipToOutline = true
            }

            // Forecast for each 3 hours
            weatherViewModel.loadForecastWeather(lat, lon, "metric").enqueue(object : retrofit2.Callback<ForecastResponseApi> {
                override fun onResponse(call: Call<ForecastResponseApi>, response: Response<ForecastResponseApi>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        blurView.visibility = View.VISIBLE
                        data?.let {
                            binding.rvForecast.apply {
                                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                                adapter = forecastHoursAdapter
                            }
                            forecastHoursAdapter.differ.submitList(it.forecastList)
                        }
                    }
                }

                override fun onFailure(call: Call<ForecastResponseApi>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to load forecast data", Toast.LENGTH_SHORT).show()
                }
            })

            // Forecast for 5 days
            weatherViewModel.loadForecastWeather(lat, lon, "metric").enqueue(object : retrofit2.Callback<ForecastResponseApi> {
                override fun onResponse(call: Call<ForecastResponseApi>, response: Response<ForecastResponseApi>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        data?.let {
                            val uniqueDaysForecast = getUniqueDaysForecast(it.forecastList)

                            binding.fiveDayForecastRecyclerView.apply {
                                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                                adapter = forecastDaysAdapter
                            }
                            forecastDaysAdapter.differ.submitList(uniqueDaysForecast)
                        }
                    }
                }

                override fun onFailure(call: Call<ForecastResponseApi>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to load 5 days forecast data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getUniqueDaysForecast(forecastList: List<ForecastResponseApi.Forecast>): List<ForecastResponseApi.Forecast> {
        val uniqueDaysMap = mutableMapOf<String, ForecastResponseApi.Forecast>()
        for (forecast in forecastList) {
            val date = SimpleDateFormat("yyyy-MM-dd").format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(forecast.dtTxt))
            if (!uniqueDaysMap.containsKey(date)) {
                uniqueDaysMap[date] = forecast
            }
        }
        return uniqueDaysMap.values.take(5)
    }

    private fun isNightNow(): Boolean = calendar.get(Calendar.HOUR_OF_DAY) >= 18

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.snow_bg
            }
            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.cloudy_bg
            }
            "09", "10", "11" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.rainy_bg
            }
            "13" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.snow_bg
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
            "01", "02", "03", "04", "09", "10", "11", "13", "50" -> initWeatherView(PrecipType.CLEAR)
        }
    }

    private fun initWeatherView(type: PrecipType) {
        binding.weatherView.apply {
            setWeatherData(type)
            angle = -20
            emissionRate = 100.0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

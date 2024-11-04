package com.example.clearsky.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.databinding.ForecastItemBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class ForecastHoursAdapter : RecyclerView.Adapter<ForecastHoursAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ForecastItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ForecastItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = differ.currentList[position]
        val binding = holder.binding

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(forecast.dtTxt)
        val calendar = Calendar.getInstance()
        calendar.time = date

        val dayOfWeekName = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> "-"
        }
        binding.nameDayTxt.text = dayOfWeekName

        val hour = calendar.get(Calendar.HOUR)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "am" else "pm"
        binding.hourTxt.text = "$hour$amPm"

        val kelvinToCelsius = { kelvin: Double -> Math.round(kelvin).toString() }

        val temperature = forecast.main?.temp?.let {
            "${kelvinToCelsius(it)}°" } ?: "-"
        binding.tempTxt.text = temperature

        val icon = when (forecast.weather.getOrNull(0)?.icon) {
            "01d", "01n" -> "sunny"
            "02d", "02n" -> "cloudy_sunny"
            "03d", "03n" -> "cloudy_sunny"
            "04d", "04n" -> "cloudy"
            "09d", "09n" -> "rainy"
            "10d", "10n" -> "rainy"
            "11d", "11n" -> "storm"
            "13d", "13n" -> "snowy"
            "50d", "50n" -> "windy"
            else -> "sunny"
        }

        val drawableResourceId: Int = binding.root.resources.getIdentifier(
            icon, "drawable", binding.root.context.packageName
        )
        Glide.with(binding.root.context)
            .load(drawableResourceId)
            .into(binding.pic)
    }

    override fun getItemCount() = differ.currentList.size

    private val differCallback = object : DiffUtil.ItemCallback<ForecastResponseApi.Forecast>() {
        override fun areItemsTheSame(oldItem: ForecastResponseApi.Forecast, newItem: ForecastResponseApi.Forecast): Boolean {
            return oldItem.dt == newItem.dt
        }

        override fun areContentsTheSame(oldItem: ForecastResponseApi.Forecast, newItem: ForecastResponseApi.Forecast): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}


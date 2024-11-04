package com.example.clearsky.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.clearsky.model.ForecastResponseApi
import com.example.clearsky.databinding.ItemFiveDayForecastBinding
import java.text.SimpleDateFormat
import java.util.Calendar


class ForecastDaysAdapter : RecyclerView.Adapter<ForecastDaysAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFiveDayForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFiveDayForecastBinding.inflate(inflater, parent, false)
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
        binding.tvDayName.text = dayOfWeekName
        val kelvinToCelsius = { kelvin: Double -> Math.round(kelvin).toString() }

        binding.tvMaxTemp.text = "Max: ${kelvinToCelsius(forecast.main.tempMax)}°C"
        binding.tvMinTemp.text = "Min: ${kelvinToCelsius(forecast.main.tempMin)}°C"
        binding.tvFeelsLike.text = "Feels like: ${kelvinToCelsius(forecast.main.feelsLike)}°C"
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

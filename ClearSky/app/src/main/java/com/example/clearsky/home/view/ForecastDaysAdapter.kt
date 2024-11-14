package com.example.clearsky.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.clearsky.R
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
            Calendar.SUNDAY -> binding.root.context.getString(R.string.sunday)
            Calendar.MONDAY -> binding.root.context.getString(R.string.monday)
            Calendar.TUESDAY -> binding.root.context.getString(R.string.tuesday)
            Calendar.WEDNESDAY -> binding.root.context.getString(R.string.wednesday)
            Calendar.THURSDAY -> binding.root.context.getString(R.string.thursday)
            Calendar.FRIDAY -> binding.root.context.getString(R.string.friday)
            Calendar.SATURDAY -> binding.root.context.getString(R.string.saturday)
            else -> "-"
        }
        binding.tvDayName.text = dayOfWeekName
        val kelvinToCelsius = { kelvin: Double -> Math.round(kelvin).toString() }

        binding.tvMaxTemp.text = binding.root.context.getString(
            R.string.max_temp, kelvinToCelsius(forecast.main.tempMax)
        )
        binding.tvMinTemp.text = binding.root.context.getString(
            R.string.min_temp, kelvinToCelsius(forecast.main.tempMin)
        )
        binding.tvFeelsLike.text = binding.root.context.getString(
            R.string.feels_like, kelvinToCelsius(forecast.main.feelsLike)
        )
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

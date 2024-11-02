package com.example.clearsky.favourite.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.clearsky.databinding.FavoriteItemBinding
import com.example.clearsky.model.CurrentResponseApi

class FavoriteAdapter(private val onItemClick: (CurrentResponseApi) -> Unit) :
    ListAdapter<CurrentResponseApi, FavoriteAdapter.FavoriteViewHolder>(DiffCallback()) {

    inner class FavoriteViewHolder(private val binding: FavoriteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: CurrentResponseApi) {
            binding.cityNameTextView.text = city.name
            binding.temperatureTextView.text =
                city.main.temp.toString() + "°C"
            binding.feelLike.text = city.main.feelsLike.toString()
            binding.root.setOnClickListener { onItemClick(city) }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = FavoriteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrentResponseApi>() {
        override fun areItemsTheSame(
            oldItem: CurrentResponseApi,
            newItem: CurrentResponseApi
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: CurrentResponseApi,
            newItem: CurrentResponseApi
        ): Boolean {
            return oldItem == newItem
        }
    }
}

package com.example.clearsky.favourite.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.clearsky.databinding.FavoriteItemBinding
import com.example.clearsky.model.CityResponseApi
import com.example.clearsky.model.CurrentResponseApi

class FavoriteAdapter(private val onItemClick: (CurrentResponseApi) -> Unit) :
    ListAdapter<CurrentResponseApi, FavoriteAdapter.FavoriteViewHolder>(DiffCallback()) {

    inner class FavoriteViewHolder(private val binding: FavoriteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: CurrentResponseApi) {
            // Set the city name
            binding.cityNameTextView.text = city.name

            // Set a static text or alternative if no temperature is available
            binding.temperatureTextView.text = "N/A" // Replace with any default text you want

            // Set click listener for the root view
            binding.root.setOnClickListener { onItemClick(city) }

            // Optional: Set a click listener for the remove button if needed
            binding.removeButton.setOnClickListener {
                // Handle remove action here
                // You may want to pass the city or index to a callback to remove it from the list
            }
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

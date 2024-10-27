package com.example.clearsky.favourite.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.clearsky.databinding.CityItemBinding
import com.example.clearsky.model.CityResponseApi

class CityAdapter : RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CityItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CityItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val city = differ.currentList[position]
        val binding = holder.binding


    }

    override fun getItemCount() = differ.currentList.size

    // Define differ and its callback
    private val differCallback = object : DiffUtil.ItemCallback<CityResponseApi.CityResponseApiItem>() {
        override fun areItemsTheSame(oldItem: CityResponseApi.CityResponseApiItem, newItem: CityResponseApi.CityResponseApiItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CityResponseApi.CityResponseApiItem, newItem: CityResponseApi.CityResponseApiItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
}


package com.example.clearsky.favourite.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.clearsky.databinding.FragmentFavouritesBinding
import com.example.clearsky.home.viewmodel.WeatherViewModel
import com.example.clearsky.home.viewmodel.WeatherViewModelFactory
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.model.repository.WeatherRepository
import com.example.clearsky.network.RemoteDataStructure

class FavouritesFragment : Fragment() {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var favoriteAdapter: FavoriteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val repository = WeatherRepository(RemoteDataStructure, WeatherLocalDataSource.getInstance(requireContext()))
        val factory = WeatherViewModelFactory(repository)
        weatherViewModel = ViewModelProvider(this, factory)[WeatherViewModel::class.java]

        // Setup RecyclerView and Adapter
        favoriteAdapter = FavoriteAdapter { city ->
            navigateToHome(city)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }

        // Observe favorites
        weatherViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            favoriteAdapter.submitList(favorites)
        }

        // Setup swipe to delete
        setupSwipeToDelete(binding.recyclerView)

        // Add favorite button
        binding.fabAddFavorite.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MAP)
        }

        // Fetch favorites when the fragment is created
        weatherViewModel.fetchFavorites()
    }

    // Handle the result from MapActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK) {
            val lat = data?.getDoubleExtra("lat", 0.0)
            val lon = data?.getDoubleExtra("lon", 0.0)

            if (lat != null && lon != null) {
                weatherViewModel.fetchCurrentWeather(lat, lon, "metric")

                    weatherViewModel.currentWeatherData.observe(viewLifecycleOwner) { city ->
                    // Add the fetched city to favorites after getting the weather data
                    weatherViewModel.addFavorite(city)
                }
            }
        }
    }


    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val city = favoriteAdapter.currentList[position]
                weatherViewModel.removeFavorite(city)
                // The favorites list will automatically update through LiveData observer.
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun navigateToHome(city: CurrentResponseApi) {
        // Code to navigate to HomeFragment with the city data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_MAP = 1 // Define request code
    }
}

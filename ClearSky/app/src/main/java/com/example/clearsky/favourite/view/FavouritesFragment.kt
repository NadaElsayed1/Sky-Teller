package com.example.clearsky.favourite.view

import android.app.Activity
import android.content.Context
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
import com.example.clearsky.R
import com.example.clearsky.databinding.FragmentFavouritesBinding
import com.example.clearsky.home.viewmodel.WeatherViewModel
import com.example.clearsky.home.viewmodel.WeatherViewModelFactory
import com.example.clearsky.model.CurrentResponseApi
import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.favourite.viewmodel.FavoriteViewModel
import com.example.clearsky.favourite.viewmodel.FavouriteViewModelFactory
import com.example.clearsky.home.view.HomeFragment
import com.example.clearsky.model.repository.WeatherRepository
import com.example.clearsky.network.RemoteDataStructure

class FavouritesFragment : Fragment() {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoriteViewModel: FavoriteViewModel
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

        val repository = WeatherRepository(RemoteDataStructure, WeatherLocalDataSource.getInstance(requireContext()))
        val factoryf = FavouriteViewModelFactory(repository)
        favoriteViewModel = ViewModelProvider(this, factoryf)[FavoriteViewModel::class.java]

        val factoryw = WeatherViewModelFactory(repository)
        weatherViewModel = ViewModelProvider(this, factoryw)[WeatherViewModel::class.java]

        favoriteAdapter = FavoriteAdapter { city ->
            navigateToHome(city)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }

        favoriteViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            favoriteAdapter.submitList(favorites)
        }

        setupSwipeToDelete(binding.recyclerView)

        binding.fabAddFavorite.setOnClickListener {
            val intent = Intent(requireContext(), MapActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MAP)
        }

        favoriteViewModel.fetchFavorites()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK) {
            val lat = data?.getDoubleExtra("lat", 0.0)
            val lon = data?.getDoubleExtra("lon", 0.0)

            if (lat != null && lon != null) {
                weatherViewModel.fetchCurrentWeather(lat, lon, "metric", "en")

                weatherViewModel.currentWeatherData.observe(viewLifecycleOwner) { city ->
                    favoriteViewModel.addFavorite(city)
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
                favoriteViewModel.removeFavorite(city)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun navigateToHome(city: CurrentResponseApi) {
        val sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("lat", city.coord.lat.toFloat())
            putFloat("lon", city.coord.lon.toFloat())
            putString("name", city.name)
            apply()
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_MAP = 1
    }
}
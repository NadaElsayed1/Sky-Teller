package com.example.clearsky

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.clearsky.favourite.view.FavouritesFragment
import com.example.clearsky.home.view.HomeFragment
import com.example.clearsky.notifications.NotificationsFragment
import com.example.clearsky.setting.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Setup BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.itemIconTintList =
            ContextCompat.getColorStateList(this, R.color.white)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> loadFragment(HomeFragment())
                R.id.favourite -> loadFragment(FavouritesFragment())
                R.id.notifications -> loadFragment(NotificationsFragment())
                R.id.setting -> loadFragment(SettingsFragment())
            }
            true
        }
    }

    // Function to load the selected fragment
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

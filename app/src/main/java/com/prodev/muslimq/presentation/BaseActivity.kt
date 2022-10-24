package com.prodev.muslimq.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.ActivityBaseBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHosFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHosFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = when (destination.id) {
                R.id.quranDetailFragment -> View.GONE
                R.id.shalatProvinceFragment -> View.GONE
                R.id.shalatCityFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
        binding.bottomNav.setupWithNavController(navController)
    }
}
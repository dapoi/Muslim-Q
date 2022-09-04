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
        binding.bottomNav.setupWithNavController(navController)
    }


    fun showBottomNavigation() {
        binding.bottomNav.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        binding.bottomNav.visibility = View.GONE
    }
}
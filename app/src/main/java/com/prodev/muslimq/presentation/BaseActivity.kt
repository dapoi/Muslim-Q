package com.prodev.muslimq.presentation

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
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
                R.id.splashScreenFragment -> View.GONE
                R.id.quranDetailFragment -> View.GONE
                R.id.shalatProvinceFragment -> View.GONE
                R.id.shalatCityFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
        binding.bottomNav.setupWithNavController(navController)
    }

    fun customSnackbar(state: Boolean, context: Context, view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT).apply {
            anchorView = binding.bottomNav
            if (state) {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context,
                        R.color.green_base
                    )
                )
            } else {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                )
            }
        }
        val layoutParams = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(60, 0, 60, 50)
        snackbar.view.layoutParams = layoutParams
        snackbar.show()
    }
}
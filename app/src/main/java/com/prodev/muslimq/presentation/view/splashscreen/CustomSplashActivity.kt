package com.prodev.muslimq.presentation.view.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.uitheme.UITheme
import com.prodev.muslimq.databinding.ActivityCustomSplashBinding
import com.prodev.muslimq.presentation.MainActivity
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class CustomSplashActivity : AppCompatActivity() {

    private var _binding: ActivityCustomSplashBinding? = null
    private val binding get() = _binding!!

    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val duration = 2000L

    override fun onStart() {
        super.onStart()
        dataStoreViewModel.getSwitchDarkMode.observe(this) { uiTheme ->
            if (uiTheme != null) {
                when (uiTheme) {
                    UITheme.LIGHT -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }

                    UITheme.DARK -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCustomSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, MainActivity::class.java).also(::startActivity)
            overridePendingTransition(R.anim.from_bottom_anim, R.anim.to_up_anim)
            finish()
        }, duration)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        dataStoreViewModel.getSwitchDarkMode.removeObservers(this)
    }
}
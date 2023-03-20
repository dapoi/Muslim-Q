package com.prodev.muslimq.presentation.view.splashscreen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prodev.muslimq.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private var _binding: ActivityOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
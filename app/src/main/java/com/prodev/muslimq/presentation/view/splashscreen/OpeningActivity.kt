package com.prodev.muslimq.presentation.view.splashscreen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prodev.muslimq.databinding.ActivityOpeningBinding

class OpeningActivity : AppCompatActivity() {

    private val binding: ActivityOpeningBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityOpeningBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
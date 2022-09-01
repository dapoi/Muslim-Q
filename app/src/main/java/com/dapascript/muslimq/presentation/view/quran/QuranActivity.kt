package com.dapascript.muslimq.presentation.view.quran

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dapascript.muslimq.R
import com.dapascript.muslimq.databinding.ActivityQuranBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuranBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHosFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHosFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }
}
package com.prodev.muslimq.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.uitheme.UITheme
import com.prodev.muslimq.databinding.ActivityMainBinding
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.service.AdzanReceiver.Companion.FROM_NOTIFICATION
import com.prodev.muslimq.service.AdzanService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHosFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHosFragment.navController
        val destinationToHideBottomnav = setOf(
            R.id.quranDetailFragment,
            R.id.shalatProvinceFragment,
            R.id.shalatCityFragment,
            R.id.bookmarkFragment,
            R.id.aboutAppFragment
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.apply {
                // give animation when hide/show bottom nav
                if (destination.id in destinationToHideBottomnav) {
                    animate().translationY(height.toFloat()).duration = 300
                    visibility = View.GONE
                } else {
                    animate().translationY(0f).duration = 300
                    visibility = View.VISIBLE
                }
            }
        }
        binding.bottomNav.setupWithNavController(navController)

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

        val fromNotif = intent.getBooleanExtra(FROM_NOTIFICATION, false)
        if (fromNotif) {
            stopService(Intent(this, AdzanService::class.java))
        }
    }

    fun customSnackbar(
        state: Boolean,
        context: Context,
        view: View,
        message: String,
        action: Boolean = false,
        isDownload: Boolean = false
    ) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).apply {
            anchorView = binding.bottomNav
            if (state) {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context, R.color.green_base
                    )
                )
            } else {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context, R.color.red
                    )
                )
            }

            setTextColor(ContextCompat.getColor(context, R.color.white_base))
            if (action) {
                setActionTextColor(ContextCompat.getColor(context, R.color.white_base))
                setAction("IZINKAN") {
                    if (isDownload) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else {
                        val intent = Intent()
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra(
                            "android.provider.extra.APP_PACKAGE", context.packageName
                        )
                        startActivity(intent)
                    }
                }
            }
        }

        val layoutParams = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(60, 0, 60, 50)
        snackbar.view.layoutParams = layoutParams
        snackbar.show()
    }
}
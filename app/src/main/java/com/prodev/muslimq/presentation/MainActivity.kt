package com.prodev.muslimq.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.uitheme.UITheme
import com.prodev.muslimq.databinding.ActivityMainBinding
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.QuranViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val quranViewModel: QuranViewModel by viewModels()
    private val dataStoreViewModel: DataStoreViewModel by viewModels()
    private val navHostFragment: NavHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }
    private val navController: NavController by lazy {
        navHostFragment.navController
    }
    private val windowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(window, binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            quranViewModel.keepSplashscreen.value ?: true
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val destinationToHideBottomnav = setOf(
            R.id.quranDetailFragment,
            R.id.shalatProvinceFragment,
            R.id.shalatCityFragment,
            R.id.bookmarkFragment,
            R.id.aboutAppFragment,
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // give animation when hide/show bottom nav
            if (destination.id in destinationToHideBottomnav) {
                animateVisibleHide(false, binding.bottomNav, binding.vDivider)
            } else {
                animateVisibleHide(true, binding.bottomNav, binding.vDivider)
            }
        }
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun animateVisibleHide(state: Boolean, bottomNav: BottomNavigationView, view: View) {
        if (state) {
            view.clearAnimation()
            bottomNav.clearAnimation()
            view.animate().translationY(0f).duration = 600
            bottomNav.animate().translationY(0f).duration = 600
        } else {
            view.clearAnimation()
            bottomNav.clearAnimation()
            view.animate().translationY(bottomNav.height.toFloat()).duration = 600
            bottomNav.animate().translationY(bottomNav.height.toFloat()).duration = 600
        }
    }

    fun customSnackbar(
        state: Boolean,
        context: Context,
        view: View,
        message: String,
        action: Boolean = false,
        toSettings: Boolean = false,
        isDetailScreen: Boolean = false
    ) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).apply {
            anchorView = binding.bottomNav
            if (state) {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context, R.color.green_button_end
                    )
                )
            } else {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context, R.color.red
                    )
                )
            }

            setTextColor(ContextCompat.getColor(context, R.color.white_header))
            if (action) {
                setActionTextColor(ContextCompat.getColor(context, R.color.white_header))
                setAction("IZINKAN") {
                    if (toSettings) {
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
        layoutParams.setMargins(60, 0, 60, if (isDetailScreen) 240 else 60)
        snackbar.view.layoutParams = layoutParams
        snackbar.show()
    }

    override fun onResume() {
        super.onResume()

        dataStoreViewModel.getSwitchDarkMode.observe(this) { uiTheme ->
            if (uiTheme != null) {
                when (uiTheme) {
                    UITheme.LIGHT -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            val exceptionFragment = destination.id != R.id.aboutAppFragment
                            windowInsetsControllerCompat.apply {
                                isAppearanceLightStatusBars = exceptionFragment
                                isAppearanceLightNavigationBars = exceptionFragment
                            }
                        }
                    }

                    UITheme.DARK -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                        windowInsetsControllerCompat.apply {
                            isAppearanceLightStatusBars = false
                            isAppearanceLightNavigationBars = false
                        }
                    }
                }
            }
        }
    }
}
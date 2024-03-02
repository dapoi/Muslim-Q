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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.UITheme
import com.prodev.muslimq.databinding.ActivityMainBinding
import com.prodev.muslimq.presentation.viewmodel.DataStoreViewModel
import com.prodev.muslimq.presentation.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val splashViewModel: SplashViewModel by viewModels()
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
        installSplashScreen().setKeepOnScreenCondition { splashViewModel.isLoading.value }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavController(navController)
        setDarkMode()
    }

    private fun setNavController(navController: NavController) {
//        val destinationToHideBottomnav = setOf(
//            R.id.quranDetailFragment,
//            R.id.shalatProvinceFragment,
//            R.id.shalatCityFragment,
//            R.id.bookmarkFragment,
//            R.id.aboutAppFragment,
//            R.id.qiblaFragment,
//            R.id.dzikirFragment,
//            R.id.asmaulHusnaFragment,
//            R.id.tasbihFragment
//        )
        val exceptFragment = setOf(
            R.id.quranFragment,
            R.id.shalatFragment,
            R.id.doaFragment,
            R.id.othersFragment
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // give animation when hide/show bottom nav
            showBottomNav(destination.id in exceptFragment, binding.bottomNav)
        }
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun setDarkMode() {
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

    private fun showBottomNav(state: Boolean, bottomNav: BottomNavigationView) {
        if (state) {
            bottomNav.clearAnimation()
            bottomNav.animate().translationY(0f).duration = 600
        } else {
            bottomNav.clearAnimation()
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
        toOtherFragment: Boolean = false
    ) {
        val textAction = if (toOtherFragment) "LIHAT" else "IZINKAN"
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).apply {
            anchorView = binding.bottomNav
            if (state) {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context, R.color.green_button
                    )
                )
            } else {
                setBackgroundTint(
                    ContextCompat.getColor(
                        context, R.color.red
                    )
                )
            }

            setTextColor(ContextCompat.getColor(context, R.color.white_always))
            if (action) {
                setActionTextColor(ContextCompat.getColor(context, R.color.white_always))
                setAction(textAction) {
                    when {
                        toOtherFragment -> {
                            navController.navigateUp()
                            val selectedId = binding.bottomNav.selectedItemId
                            if (selectedId == R.id.othersFragment) {
                                navController.popBackStack(
                                    destinationId = R.id.othersFragment,
                                    inclusive = false
                                )
                            } else {
                                binding.bottomNav.selectedItemId = R.id.othersFragment
                            }
                        }

                        toSettings -> {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", context.packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }

                        else -> {
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
        }

        val layoutParams = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(60, 0, 60, if (toOtherFragment) 240 else 60)
        snackbar.view.layoutParams = layoutParams
        snackbar.show()
    }

    fun showOverlay(state: Boolean) {
        binding.gestureOverlay.isVisible = state
    }
}
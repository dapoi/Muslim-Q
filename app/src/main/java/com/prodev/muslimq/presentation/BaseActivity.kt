package com.prodev.muslimq.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.ActivityBaseBinding
import com.prodev.muslimq.service.AdzanReceiver.Companion.FROM_NOTIFICATION
import com.prodev.muslimq.service.AdzanService
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
        val destinationToHideBottomnav = setOf(
            R.id.splashScreenFragment,
            R.id.quranDetailFragment,
            R.id.shalatProvinceFragment,
            R.id.shalatCityFragment,
            R.id.bookmarkFragment,
            R.id.aboutAppFragment
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = if (destination.id in destinationToHideBottomnav) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        binding.bottomNav.setupWithNavController(navController)

        val fromNotif = intent.getBooleanExtra(FROM_NOTIFICATION, false)
        if (fromNotif) {
            val serviceIntent = Intent(this, AdzanService::class.java)
            stopService(serviceIntent)
        }
    }

    fun customSnackbar(
        state: Boolean,
        context: Context,
        view: View,
        message: String,
        action: Boolean = false,
        toBookmark: Boolean = false
    ) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).apply {
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

            setTextColor(ContextCompat.getColor(context, R.color.white))
            val goToBookmark = if (toBookmark) "LIHAT" else "IZINKAN"
            if (action) {
                setActionTextColor(ContextCompat.getColor(context, R.color.white))
                setAction(goToBookmark) {
                    if (toBookmark) {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.bookmarkFragment)
                    } else {
                        val intent = Intent()
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra(
                            "android.provider.extra.APP_PACKAGE",
                            context.packageName
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
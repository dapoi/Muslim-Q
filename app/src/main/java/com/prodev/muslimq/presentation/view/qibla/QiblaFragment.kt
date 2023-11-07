package com.prodev.muslimq.presentation.view.qibla

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.prodev.muslimq.R
import com.prodev.muslimq.databinding.FragmentQiblaBinding
import com.prodev.muslimq.helper.Compass
import com.prodev.muslimq.helper.SOTWFormatter
import com.prodev.muslimq.helper.vibrateApp
import com.prodev.muslimq.presentation.view.BaseFragment
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaFragment : BaseFragment<FragmentQiblaBinding>(FragmentQiblaBinding::inflate) {

    private lateinit var compass: Compass
    private lateinit var sotwFormatter: SOTWFormatter
    private var currentAzimuth = 0f

    private val args: QiblaFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userLat = args.latUser.toDouble()
        val userLocation = args.locationUser

        binding.apply {
            tvYourLocation.text = userLocation.joinToString(", ")
            ivBack.setOnClickListener { findNavController().popBackStack() }
        }

        setupCompass(userLat)
    }

    private fun setupCompass(userLat: Double) {
        compass = Compass(requireContext())
        sotwFormatter = SOTWFormatter()
        val compassListener = object : Compass.CompassListener {
            override fun onNewAzimuth(azimuth: Float) {
                viewLifecycleOwner.lifecycleScope.launch {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        adjustArrow(azimuth, userLat)
                    }
                }
            }

            override fun onAccuracyChanged(accuracy: Int) {
                // do nothing
            }
        }
        compass.setListener(compassListener)
    }

    private fun adjustArrow(azimuth: Float, userLat: Double) {
        // ka'bah Position https://www.latlong.net/place/kaaba-mecca-saudi-arabia-12639.html
        val kaabaLng = 39.826206
        val kaabaLat = Math.toRadians(21.422487)

        val myLatRad = Math.toRadians(userLat)
        val longDiff = Math.toRadians(kaabaLng - 107.446274)
        val y = sin(longDiff) * cos(kaabaLat)
        val x = cos(myLatRad) * sin(kaabaLat) - sin(myLatRad) * cos(kaabaLat) * cos(longDiff)
        val result = (Math.toDegrees(atan2(y, x)) + 360) % 360
        val sotwNumeric = extractNumericValue(sotwFormatter.format(azimuth))
        val sotwNumericRange = sotwNumeric?.minus(8)!!..sotwNumeric.plus(8)
        val validRange = result.toInt() in sotwNumericRange

        binding.apply {
            tvAzimuth.text = sotwFormatter.format(azimuth)
            ivArrowFalse.isVisible = !validRange
            tvQibla.text = if (validRange) resources.getString(R.string.found_qibla)
            else resources.getString(R.string.find_qibla)

            val vibrator = vibrateApp(requireContext())
            val durationDone = 100L
            if (validRange) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationDone,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION") vibrator.vibrate(durationDone)
                }
            }
        }

        val an: Animation = RotateAnimation(
            -currentAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        an.duration = 10
        an.repeatCount = 0
        an.fillAfter = true
        binding.clDirection.startAnimation(an)
    }

    private fun extractNumericValue(inputString: String): Int? {
        val regex = """\d+""".toRegex()
        val matchResult = regex.find(inputString)
        return matchResult?.value?.toIntOrNull()
    }

    override fun onResume() {
        super.onResume()
        compass.start()
    }

    override fun onStop() {
        super.onStop()
        compass.stop()
    }

    override fun onDestroyView() {
        compass.stop()
        super.onDestroyView()
    }
}
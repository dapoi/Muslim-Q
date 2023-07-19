package com.prodev.muslimq.presentation.view.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.animation.RotateAnimation
import androidx.navigation.fragment.findNavController
import com.prodev.muslimq.databinding.FragmentQiblaBinding
import com.prodev.muslimq.presentation.view.BaseFragment
import kotlin.math.roundToInt

class QiblaFragment : BaseFragment<FragmentQiblaBinding>(FragmentQiblaBinding::inflate),
    SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var currentDegree = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onSensorChanged(event: SensorEvent) {
        val degree = event.values[0].roundToInt().toFloat()
        val animation = RotateAnimation(
            currentDegree,
            -degree,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        )
        animation.duration = 210
        animation.fillAfter = true
        binding.clDirectionParent.startAnimation(animation)
        currentDegree = -degree
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.let {
            sensor = it.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            it.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    companion object {
        const val USER_LATITUDE = "user_latitude"
        const val USER_LONGITUDE = "user_longitude"
    }
}
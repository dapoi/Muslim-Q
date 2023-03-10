package com.prodev.muslimq.presentation.view.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import com.prodev.muslimq.databinding.FragmentQiblaBinding
import kotlin.math.roundToInt

class QiblaFragment : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentQiblaBinding

    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var currentDegree = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQiblaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
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
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
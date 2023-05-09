package com.prodev.muslimq.service.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.LiveData

class GPSStatusListener(private val context: Context) : LiveData<Boolean>() {

    override fun onActive() {
        registerReceiver()
        checkStatusGPS()
    }

    override fun onInactive() {
        unregisterReceiver()
    }

    private val gpsStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkStatusGPS()
        }
    }

    private fun checkStatusGPS() = if (isLocationEnabled()) {
        postValue(true)
    } else {
        postValue(false)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun registerReceiver() = context.registerReceiver(
        gpsStatusReceiver,
        IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
    )

    private fun unregisterReceiver() = context.unregisterReceiver(gpsStatusReceiver)
}
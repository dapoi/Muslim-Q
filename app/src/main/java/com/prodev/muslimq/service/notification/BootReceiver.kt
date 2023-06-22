package com.prodev.muslimq.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.data.source.local.database.ShalatDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var shalatDao: ShalatDao

    @Inject
    lateinit var dataStorePreference: DataStorePreference

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                println("Boot up berhasil")
                dataStorePreference.getCityAndCountryData.collect {
                    val shalatTimes = shalatDao.getShalatDailyByCity(it.first, it.second).first()
                    val listAdzanTime = mapOf(
                        "Adzan Shubuh" to shalatTimes.shubuh,
                        "Adzan Dzuhur" to shalatTimes.dzuhur,
                        "Adzan Ashar" to shalatTimes.ashar,
                        "Adzan Maghrib" to shalatTimes.maghrib,
                        "Adzan Isya" to shalatTimes.isya
                    )

                    val adzanNames = listAdzanTime.keys.toList()
                    adzanNames.forEach { adzanName ->
                        dataStorePreference.saveSwitchState(adzanName, false)
                    }
                }
            }
        }
    }
}
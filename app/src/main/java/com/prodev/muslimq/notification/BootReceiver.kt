package com.prodev.muslimq.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.utils.AdzanConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStorePreference: DataStorePreference

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {

                listOf(
                    AdzanConstants.KEY_ADZAN_SHUBUH,
                    AdzanConstants.KEY_ADZAN_DZUHUR,
                    AdzanConstants.KEY_ADZAN_ASHAR,
                    AdzanConstants.KEY_ADZAN_MAGHRIB,
                    AdzanConstants.KEY_ADZAN_ISYA
                ).map { adzanName ->
                    dataStorePreference.saveSwitchState(adzanName, false)
                }

                context.reminderNotification()
            }
        }
    }

    // reminder user to set adzan again
    private fun Context.reminderNotification() {
        val notificationManager = this.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val pendingIntent = NavDeepLinkBuilder(this)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.shalatFragment)
            .setArguments(Bundle().apply {
                putBoolean(AdzanConstants.FROM_NOTIFICATION, true)
            })
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, AdzanConstants.CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_notif_circle)
            .setContentTitle("Aktifkan Pengingat Shalat")
            .setContentText("Mohon untuk mengaktifkan kembali pengingat shalat setelah me-restart perangkat.")
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AdzanConstants.CHANNEL_ID_REMINDER,
                AdzanConstants.CHANNEL_NAME_REMINDER,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }
            notification.setChannelId(AdzanConstants.CHANNEL_ID_REMINDER)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(10, notification.build())
    }
}
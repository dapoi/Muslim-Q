package com.prodev.muslimq.presentation.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.prodev.muslimq.R
import com.prodev.muslimq.presentation.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val adzanName = intent.getStringExtra(TITLE)
        val isShubuh = intent.getBooleanExtra(IS_SHUBUH, false)

        showNotification(context, adzanName, isShubuh)
    }

    private fun showNotification(
        context: Context, adzanName: String?, isShubuh: Boolean
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val adzanSound = if (isShubuh) {
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.adzan_shubuh)
        } else {
            Uri.parse("android.resource://" + context.packageName + "/" + R.raw.adzan_regular)
        }

        val mediaPlayer = MediaPlayer.create(context, adzanSound)
        mediaPlayer.start()

        val notificationIntent = Intent(context, BaseActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            ID_REPEATING,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notif_circle).setContentTitle(adzanName)
                .setContentText("Waktunya shalat").setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

            builder.setChannelId(CHANNEL_ID)

            notificationManager.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notificationManager.notify(ID_REPEATING, notification)
    }

    fun scheduleAdzan(
        context: Context, prayerTimes: String, prayName: String, isShubuh: Boolean = false
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(TITLE, prayName)
        intent.putExtra(IS_SHUBUH, isShubuh)

        val adzanTime =
            prayerTimes.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, adzanTime[0].toInt())
            set(Calendar.MINUTE, adzanTime[1].toInt())
            set(Calendar.SECOND, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ID_REPEATING,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_HALF_HOUR,
            pendingIntent
        )
    }

    fun removeAdzan(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ID_REPEATING,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val CHANNEL_ID = "Channel_101"
        const val CHANNEL_NAME = "AlarmManager channel"
        const val ID_REPEATING = 101

        const val TITLE = "title"
        const val IS_SHUBUH = "is_shubuh"
    }
}
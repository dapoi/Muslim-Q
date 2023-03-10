package com.prodev.muslimq.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.prodev.muslimq.R
import com.prodev.muslimq.presentation.BaseActivity
import java.util.*
import java.util.concurrent.TimeUnit

class AdzanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val adzanData = when (intent.getIntExtra(ADZAN_CODE, 0)) {
            "Adzan Shubuh".hashCode() -> "Adzan Shubuh"
            "Adzan Dzuhur".hashCode() -> "Adzan Dzuhur"
            "Adzan Ashar".hashCode() -> "Adzan Ashar"
            "Adzan Maghrib".hashCode() -> "Adzan Maghrib"
            "Adzan Isya".hashCode() -> "Adzan Isya"
            else -> ""
        }

        createNotification(context, adzanData)
        val serviceIntent = Intent(context, AdzanService::class.java).apply {
            putExtra(ADZAN_CODE, adzanData)
        }
        context.startService(serviceIntent)
    }

    private fun createNotification(context: Context, adzanData: String) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, BaseActivity::class.java).apply {
                putExtra(FROM_NOTIFICATION, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notif_circle)
            .setContentTitle(adzanData)
            .setContentText("Waktunya Shalat")
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Adzan Channel", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }
            notification.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(adzanData.hashCode(), notification.build())
    }

    fun setAdzanReminder(context: Context, adzanTime: String, adzanName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AdzanReceiver::class.java).apply {
            putExtra(ADZAN_CODE, adzanName.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            adzanName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm time
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()
        val adzanTimeParts = adzanTime.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, adzanTimeParts[0].toInt())
        calendar.set(Calendar.MINUTE, adzanTimeParts[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        var alarmTime = calendar.timeInMillis

        // If the alarm time is before the current time, add a day to the alarm time
        if (alarmTime < currentTime) {
            alarmTime += TimeUnit.DAYS.toMillis(1)
        }

        // AlarmManager.RTC_WAKEUP: Alarm will go off even if the device is in sleep mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )
    }

    fun cancelAdzanReminder(context: Context, adzanName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AdzanReceiver::class.java).apply {
            putExtra(ADZAN_CODE, adzanName.hashCode())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            adzanName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val ADZAN_CODE = "adzan_code"
        const val FROM_NOTIFICATION = "from_notification"
        private const val CHANNEL_ID = "prayer_time_channel"
    }
}

package com.prodev.muslimq.service.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.prodev.muslimq.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdzanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val adzanName = intent.getStringExtra(ADZAN_NAME)
        val adzanCode = intent.getIntExtra(ADZAN_CODE, 0)
        val adzanTime = intent.getStringExtra(ADZAN_TIME)
        val isShubuh = intent.getBooleanExtra(IS_SHUBUH, false)

        createNotification(context, adzanName!!, adzanCode)

        // Check if the AdzanService is already running
        if (AdzanService.isRunning()) {
            return
        }

        // Start the AdzanService
        val serviceIntent = Intent(context, AdzanService::class.java).apply {
            putExtra(IS_SHUBUH, isShubuh)
        }
        context.startService(serviceIntent)

        // Reschedule the alarm for the next day
        val newAdzanTime = adzanTime?.let { getNextDayAdzanTime(it) }
        setAdzanReminder(context, newAdzanTime!!, adzanName, adzanCode, isShubuh)
    }

    private fun getNextDayAdzanTime(adzanTime: String): String {
        val calendar = Calendar.getInstance()
        val adzanTimeParts = adzanTime.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, adzanTimeParts[0].toInt())
        calendar.set(Calendar.MINUTE, adzanTimeParts[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    private fun createNotification(context: Context, adzanName: String, adzanCode: Int) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val notificationIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.bottom_nav)
            .setDestination(R.id.shalatFragment)
            .setArguments(Bundle().apply {
                putBoolean(FROM_NOTIFICATION, true)
            })
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, getChannelId(adzanCode))
            .setSmallIcon(R.drawable.ic_notif_circle)
            .setContentIntent(notificationIntent)
            .setContentTitle(adzanName)
            .setWhen(System.currentTimeMillis())
            .setContentText("Waktunya Menunaikan Shalat ${adzanName.split(" ").getOrNull(1)}")
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getChannelId(adzanCode),
                getChannelName(adzanCode),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }
            notification.setChannelId(getChannelId(adzanCode))
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(adzanCode, notification.build())
    }

    private fun getChannelId(adzanCode: Int): String {
        return when (adzanCode) {
            1 -> CHANNEL_ID_SHUBUH
            2 -> CHANNEL_ID_DZUHUR
            3 -> CHANNEL_ID_ASHAR
            4 -> CHANNEL_ID_MAGHRIB
            5 -> CHANNEL_ID_ISYA
            else -> throw IllegalArgumentException("Unknown adzan code")
        }
    }

    private fun getChannelName(adzanCode: Int): String {
        return when (adzanCode) {
            1 -> "Adzan Shubuh Channel"
            2 -> "Adzan Dzuhur Channel"
            3 -> "Adzan Ashar Channel"
            4 -> "Adzan Maghrib Channel"
            5 -> "Adzan Isya Channel"
            else -> throw IllegalArgumentException("Unknown adzan code")
        }
    }

    fun setAdzanReminder(
        context: Context,
        adzanTime: String,
        adzanName: String,
        adzanCode: Int,
        isShubuh: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AdzanReceiver::class.java).apply {
            putExtra(ADZAN_NAME, adzanName)
            putExtra(ADZAN_CODE, adzanCode)
            putExtra(ADZAN_TIME, adzanTime)
            putExtra(IS_SHUBUH, isShubuh)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            adzanCode,
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

        // Check if the alarm time is in the past, if so, add a day
        if (calendar.timeInMillis < currentTime) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set the alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelAdzanReminder(context: Context, adzanCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AdzanReceiver::class.java).apply {
            putExtra(ADZAN_CODE, adzanCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            adzanCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val ADZAN_CODE = "adzan_code"
        const val ADZAN_NAME = "adzan_name"
        const val ADZAN_TIME = "adzan_time"
        const val IS_SHUBUH = "is_shubuh"

        const val FROM_NOTIFICATION = "from_notification"
        private const val CHANNEL_ID_SHUBUH = "channel_shubuh"
        private const val CHANNEL_ID_DZUHUR = "channel_dzuhur"
        private const val CHANNEL_ID_ASHAR = "channel_ashar"
        private const val CHANNEL_ID_MAGHRIB = "channel_maghrib"
        private const val CHANNEL_ID_ISYA = "channel_isya"
    }
}
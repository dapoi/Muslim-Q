package com.prodev.muslimq.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.utils.AdzanConstants.ADZAN_CODE
import com.prodev.muslimq.core.utils.AdzanConstants.ADZAN_NAME
import com.prodev.muslimq.core.utils.AdzanConstants.ADZAN_TIME
import com.prodev.muslimq.core.utils.AdzanConstants.IS_SHUBUH
import com.prodev.muslimq.core.utils.AdzanConstants.MUADZIN_REGULAR
import com.prodev.muslimq.core.utils.AdzanConstants.MUADZIN_SHUBUH
import com.prodev.muslimq.core.utils.getChannelId
import com.prodev.muslimq.core.utils.getChannelName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AdzanReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStorePreference: DataStorePreference

    override fun onReceive(context: Context, intent: Intent) {
        val adzanName = intent.getStringExtra(ADZAN_NAME).toString()
        val adzanCode = intent.getIntExtra(ADZAN_CODE, 0)
        val adzanTime = intent.getStringExtra(ADZAN_TIME)
        val isShubuh = intent.getBooleanExtra(IS_SHUBUH, false)

        if (AdzanService.isRunning()) return

        CoroutineScope(SupervisorJob()).launch {
            dataStorePreference.getAdzanSoundStateAndMuadzin.collect { data ->
                val isSoundActive = data.first
                val muadzinRegular = data.second
                val muadzinShubuh = data.third
                if (isSoundActive) {
                    // Start the AdzanService
                    val serviceIntent = Intent(context, AdzanService::class.java).apply {
                        putExtra(ADZAN_NAME, adzanName)
                        putExtra(ADZAN_CODE, adzanCode)
                        putExtra(IS_SHUBUH, isShubuh)
                        putExtra(MUADZIN_REGULAR, muadzinRegular)
                        putExtra(MUADZIN_SHUBUH, muadzinShubuh)
                    }
                    context.startService(serviceIntent)
                } else {
                    // Show notif with default ringtone
                    context.showDefaultNotification(adzanName, adzanCode)
                }
            }
        }

        // Reschedule the alarm for the next day
        val newAdzanTime = adzanTime?.let { getNextDayAdzanTime(it) }
        if (newAdzanTime != null) {
            setAdzanReminder(context, newAdzanTime, adzanName, adzanCode, isShubuh)
        }
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

    private fun Context.showDefaultNotification(adzanName: String, adzanCode: Int) {
        val notificationManager = this.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val notification = NotificationCompat.Builder(this, getChannelId(adzanCode))
            .setSmallIcon(R.drawable.ic_notif_circle)
            .setContentTitle(adzanName)
            .setContentText("Waktunya Menunaikan Shalat ${adzanName.split(" ").getOrNull(1)}")
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getChannelId(adzanCode),
                getChannelName(adzanCode),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }
            notification.setChannelId(getChannelId(adzanCode))
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(adzanCode, notification.build())
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

        if (pendingIntent != null) {
            pendingIntent.cancel()
            alarmManager.cancel(pendingIntent)
        }
    }
}
package com.prodev.muslimq.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.prodev.muslimq.R
import com.prodev.muslimq.core.data.preference.DataStorePreference
import com.prodev.muslimq.core.utils.AdzanConstants.ADZAN_CODE
import com.prodev.muslimq.core.utils.AdzanConstants.ADZAN_LOCATION
import com.prodev.muslimq.core.utils.AdzanConstants.ADZAN_NAME
import com.prodev.muslimq.core.utils.AdzanConstants.ADZAN_TIME
import com.prodev.muslimq.core.utils.AdzanConstants.IS_SHUBUH
import com.prodev.muslimq.core.utils.AdzanConstants.MUADZIN_REGULAR
import com.prodev.muslimq.core.utils.AdzanConstants.MUADZIN_SHUBUH
import com.prodev.muslimq.helper.getChannelId
import com.prodev.muslimq.helper.getChannelName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AdzanReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStorePreference: DataStorePreference

    override fun onReceive(context: Context, intent: Intent) {
        val adzanName = intent.getStringExtra(ADZAN_NAME)
        val adzanCode = intent.getIntExtra(ADZAN_CODE, 0)
        val adzanTime = intent.getStringExtra(ADZAN_TIME)
        val adzanLocation = intent.getStringExtra(ADZAN_LOCATION)
        val isShubuh = intent.getBooleanExtra(IS_SHUBUH, false)

        if (AdzanService.isRunning()) return

        if (adzanName != null && adzanTime != null && adzanLocation != null) {
            CoroutineScope(Dispatchers.IO).launch {
                dataStorePreference.getAdzanSoundStateAndMuadzin.first().let { data ->
                    val (isSoundActive, muadzinRegular, muadzinShubuh) = data
                    if (isSoundActive) {
                        // Start the AdzanService
                        val serviceIntent = Intent(context, AdzanService::class.java).apply {
                            putExtra(ADZAN_NAME, adzanName)
                            putExtra(ADZAN_CODE, adzanCode)
                            putExtra(ADZAN_LOCATION, adzanLocation)
                            putExtra(IS_SHUBUH, isShubuh)
                            putExtra(MUADZIN_REGULAR, muadzinRegular)
                            putExtra(MUADZIN_SHUBUH, muadzinShubuh)
                        }
                        context.startService(serviceIntent)
                    } else {
                        // Show notif with default ringtone
                        context.showDefaultNotification(adzanName, adzanCode, adzanLocation)
                    }
                }
            }

            // Reschedule the alarm for the next day
            val nextAdzanTime = getNextAdzanTime(adzanTime)
            setAdzanReminder(context, nextAdzanTime, adzanName, adzanCode, adzanLocation, isShubuh)
        }
    }

    private fun getNextAdzanTime(adzanTime: String): String {
        val adzanTimeParts = adzanTime.split(":")
        val adzanHour = adzanTimeParts[0].toInt()
        val adzanMinute = adzanTimeParts[1].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, adzanHour)
        calendar.set(Calendar.MINUTE, adzanMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        return "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
    }

    private fun Context.showDefaultNotification(
        adzanName: String,
        adzanCode: Int,
        adzanLocation: String
    ) {
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val description = buildString {
            append("Waktunya Menunaikan Shalat ")
            append(adzanName.split(" ").getOrNull(1))
            append(" untuk wilayah ")
            append(adzanLocation)
        }
        val notification = NotificationCompat.Builder(this, getChannelId(adzanCode))
            .setSmallIcon(R.drawable.ic_notif_circle)
            .setContentTitle(adzanName)
            .setContentText(description)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle())
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
        adzanLocation: String,
        isShubuh: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AdzanReceiver::class.java).apply {
            putExtra(ADZAN_NAME, adzanName)
            putExtra(ADZAN_CODE, adzanCode)
            putExtra(ADZAN_TIME, adzanTime)
            putExtra(ADZAN_LOCATION, adzanLocation)
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
        if (calendar.timeInMillis <= currentTime) calendar.add(Calendar.DAY_OF_YEAR, 1)

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
}
package com.prodev.muslimq.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.prodev.muslimq.R
import com.prodev.muslimq.presentation.BaseActivity

class AdzanReminderWorker(
    context: Context, workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        val timeNow = inputData.getString("timeNow")
        val title = inputData.getString("title")
        val time = inputData.getString("time")
        val isShubuh = inputData.getBoolean("isShubuh", false)

        if (timeNow == time) createNotification(title, isShubuh)

        return Result.success()
    }

    private fun createNotification(title: String?, shubuh: Boolean) {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, BaseActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notif_circle)
            .setContentTitle("Adzan $title")
            .setContentText("Waktunya Shalat")
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        MediaPlayer.create(
            applicationContext, if (shubuh) R.raw.adzan_shubuh else R.raw.adzan_regular
        ).apply { isLooping = false }.start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Adzan Channel", NotificationManager.IMPORTANCE_HIGH
            )
            notification.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notification.build())
    }

    companion object {
        private const val CHANNEL_ID = "prayer_time_channel"
        private const val notificationId = 0
    }
}
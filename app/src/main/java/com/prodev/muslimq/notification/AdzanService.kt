package com.prodev.muslimq.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.prodev.muslimq.R
import com.prodev.muslimq.core.utils.Constant
import com.prodev.muslimq.core.utils.getChannelId
import com.prodev.muslimq.core.utils.getChannelName
import okio.IOException

class AdzanService : Service() {

    private lateinit var notificationManager: NotificationManager
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private var isServiceRunning = false

        fun isRunning(): Boolean {
            return isServiceRunning
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_ADZAN_SERVICE") {
            mediaPlayer?.stop()
            stopSelf()
            isServiceRunning = false
            return START_NOT_STICKY
        }

        if (isServiceRunning) {
            // Service is already running, ignore the new request
            return START_NOT_STICKY
        }

        val adzanName = intent?.getStringExtra(Constant.ADZAN_NAME)
        val adzanCode = intent?.getIntExtra(Constant.ADZAN_CODE, 0)
        val isShubuh = intent?.getBooleanExtra(Constant.KEY_ADZAN_SHUBUH, false) ?: false
        val audio = if (isShubuh) R.raw.adzan_shubuh else R.raw.adzan_regular
        val attribute = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(attribute)

        val afd = applicationContext.resources.openRawResourceFd(audio)
        try {
            mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaPlayer?.setOnPreparedListener {
            it.isLooping = false
            it.start()
        }

        mediaPlayer?.setOnCompletionListener {
            stopSelf()
            isServiceRunning = false
        }

        applicationContext.showServiceNotification(adzanName.toString(), adzanCode!!)

        isServiceRunning = true

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun Context.showServiceNotification(adzanName: String, adzanCode: Int) {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val stopIntent = Intent(this, AdzanService::class.java).apply {
            action = "STOP_ADZAN_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            adzanCode,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, getChannelId(adzanCode))
            .setSmallIcon(R.drawable.ic_notif_circle)
            .setContentTitle(adzanName)
            .setContentText("Waktunya Menunaikan Shalat ${adzanName.split(" ").getOrNull(1)}")
            .setSound(null)
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDeleteIntent(stopPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getChannelId(adzanCode),
                getChannelName(adzanCode),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            }
            notification.setChannelId(getChannelId(adzanCode))
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(adzanCode, notification.build())
    }
}

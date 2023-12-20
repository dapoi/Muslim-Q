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
import com.prodev.muslimq.core.utils.AdzanConstants
import com.prodev.muslimq.helper.getChannelId
import com.prodev.muslimq.helper.getChannelName
import okio.IOException

class AdzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private var isServiceRunning = false

        fun isRunning(): Boolean {
            return isServiceRunning
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == AdzanConstants.STOP_ADZAN) {
            stopAudio()
            stopSelf()
            return START_NOT_STICKY
        }

        if (isServiceRunning) {
            // Service is already running, ignore the new request
            return START_NOT_STICKY
        }

        val adzanName = intent?.getStringExtra(AdzanConstants.ADZAN_NAME)
        val adzanCode = intent?.getIntExtra(AdzanConstants.ADZAN_CODE, 0)
        val adzanLocation = intent?.getStringExtra(AdzanConstants.ADZAN_LOCATION)
        val muadzinRegular = intent?.getStringExtra(AdzanConstants.MUADZIN_REGULAR).toString()
        val muadzinShubuh = intent?.getStringExtra(AdzanConstants.MUADZIN_SHUBUH).toString()
        val isShubuh = intent?.getBooleanExtra(AdzanConstants.IS_SHUBUH, false) ?: false
        val shubuhAudio = when {
            "Hazim" in muadzinShubuh -> R.raw.abu_hazim_shubuh
            else -> R.raw.salah_mansoor_shubuh
        }
        val regularAudio = when {
            "Ali" in muadzinRegular -> R.raw.ali_ahmed_mulla
            "Hafiz" in muadzinRegular -> R.raw.hafiz_mustafa
            else -> R.raw.mishary_rasyid
        }
        val audio = if (isShubuh) shubuhAudio else regularAudio
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
            stopAudio()
            stopSelf()
        }

        applicationContext.showServiceNotification(
            adzanName.toString(),
            adzanCode!!,
            adzanLocation.toString()
        )

        isServiceRunning = true

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                release()
                mediaPlayer = null
            }
        }

        isServiceRunning = false
    }

    private fun Context.showServiceNotification(
        adzanName: String,
        adzanCode: Int,
        adzanLocation: String
    ) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val stopIntent = Intent(this, AdzanService::class.java).apply {
            action = AdzanConstants.STOP_ADZAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            adzanCode,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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

package com.prodev.muslimq.notification

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import com.prodev.muslimq.R
import com.prodev.muslimq.notification.AdzanReceiver.Companion.IS_SHUBUH

class AdzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private var isServiceRunning = false

        fun isRunning(): Boolean {
            return isServiceRunning
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isServiceRunning) {
            // Service is already running, ignore the new request
            return START_NOT_STICKY
        }

        val isShubuh = intent?.getBooleanExtra(IS_SHUBUH, false) ?: false
        val audio = if (isShubuh) {
            Uri.parse(buildString {
                append(ContentResolver.SCHEME_ANDROID_RESOURCE)
                append("://")
                append(packageName)
                append("/")
                append(R.raw.adzan_shubuh)
            })
        } else {
            Uri.parse(buildString {
                append(ContentResolver.SCHEME_ANDROID_RESOURCE)
                append("://")
                append(packageName)
                append("/")
                append(R.raw.adzan_regular)
            })
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(applicationContext, audio)
            setOnCompletionListener {
                stop()
                release()
                stopSelf()
                isServiceRunning = false
            }
            prepare()
            isLooping = false
            start()
        }

        isServiceRunning = true

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

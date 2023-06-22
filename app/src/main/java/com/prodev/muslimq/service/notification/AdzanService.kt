package com.prodev.muslimq.service.notification

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.prodev.muslimq.R
import com.prodev.muslimq.service.notification.AdzanReceiver.Companion.IS_SHUBUH

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

        intent?.getBooleanExtra(IS_SHUBUH, false)?.let { isShubuh ->
            val audio = if (isShubuh) R.raw.adzan_shubuh else R.raw.adzan_regular

            mediaPlayer = MediaPlayer.create(this, audio).apply {
                setOnCompletionListener {
                    stop()
                    release()
                    stopSelf()
                    isServiceRunning = false
                }
                start()
            }

            isServiceRunning = true
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

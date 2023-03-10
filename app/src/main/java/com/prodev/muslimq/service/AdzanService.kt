package com.prodev.muslimq.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.prodev.muslimq.R
import com.prodev.muslimq.service.AdzanReceiver.Companion.ADZAN_CODE

class AdzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val adzanData = intent?.getIntExtra(ADZAN_CODE, 0)
        val adzanAudio = if (adzanData == "Adzan Shubuh".hashCode()) {
            R.raw.adzan_shubuh
        } else {
            R.raw.adzan_regular
        }
        mediaPlayer = MediaPlayer.create(this, adzanAudio).apply {
            start()
            setOnCompletionListener {
                stopSelf()
                mediaPlayer?.release()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}
package com.example.service

import android.app.Notification
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
import com.example.MainActivity
import com.example.R
import com.example.data.local.UserPreferences

class AdhanPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val CHANNEL_ID = "adhan_playback_channel"
        const val NOTIFICATION_ID = 4001
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopPlayback()
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(AdhanAlarmScheduler.EXTRA_PRAYER_NAME) ?: getString(R.string.prayer_fajr)
        val isFajr = intent?.getBooleanExtra(AdhanAlarmScheduler.EXTRA_IS_FAJR, false) ?: false

        startForeground(NOTIFICATION_ID, buildPlaybackNotification(prayerName))

        playAudio(isFajr)

        return START_NOT_STICKY
    }

    private fun playAudio(isFajr: Boolean) {
        val prefs = UserPreferences(this)
        val selectedVoice = prefs.getMuezzinSound()

        val rawResId = when {
            isFajr -> R.raw.adhan_fajr
            selectedVoice == "madinah" -> R.raw.adhan_madinah
            selectedVoice == "takbeer" -> R.raw.takbeer
            else -> R.raw.adhan_makkah
        }

        stopPlayback()

        try {
            mediaPlayer = MediaPlayer.create(this, rawResId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setOnCompletionListener {
                    stopPlayback()
                    stopSelf()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    private fun buildPlaybackNotification(prayerName: String): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdhanPlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.time_for, prayerName))
            .setContentText(getString(R.string.dedication_arabic))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.stop_adhan),
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Adhan Prayer Audio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Adhan prayer call playback"
                setSound(null, null)
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

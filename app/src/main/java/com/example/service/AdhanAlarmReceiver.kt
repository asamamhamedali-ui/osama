package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class AdhanAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ALARM_CHANNEL_ID = "adhan_prayer_alarm_channel"
        const val NOTIFICATION_ID = 5001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == AdhanAlarmScheduler.ACTION_DISMISS_ADHAN) {
            val stopServiceIntent = Intent(context, AdhanPlayerService::class.java).apply {
                this.action = AdhanPlayerService.ACTION_STOP
            }
            context.startService(stopServiceIntent)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        val prayerName = intent.getStringExtra(AdhanAlarmScheduler.EXTRA_PRAYER_NAME)
            ?: context.getString(R.string.prayer_fajr)
        val isFajr = intent.getBooleanExtra(AdhanAlarmScheduler.EXTRA_IS_FAJR, false)

        createNotificationChannel(context)

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            this.action = AdhanAlarmScheduler.ACTION_DISMISS_ADHAN
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.time_for, prayerName))
            .setContentText(context.getString(R.string.dedication_arabic))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.stop_adhan),
                dismissPendingIntent
            )
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        // Launch foreground audio service
        val serviceIntent = Intent(context, AdhanPlayerService::class.java).apply {
            putExtra(AdhanAlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AdhanAlarmScheduler.EXTRA_IS_FAJR, isFajr)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Reschedule for next prayers
        AdhanAlarmScheduler.scheduleDailyPrayers(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Adhan Prayer Notifications"
                enableVibration(true)
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

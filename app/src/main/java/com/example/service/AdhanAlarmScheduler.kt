package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.calculation.AstronomicalPrayerCalculator
import com.example.data.local.UserPreferences
import com.example.data.model.PrayerType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object AdhanAlarmScheduler {
    private const val TAG = "AdhanScheduler"
    const val ACTION_TRIGGER_ADHAN = "com.example.ACTION_TRIGGER_ADHAN"
    const val ACTION_DISMISS_ADHAN = "com.example.ACTION_DISMISS_ADHAN"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_IS_FAJR = "extra_is_fajr"

    fun scheduleDailyPrayers(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val prefs = UserPreferences(context)

        val schedule = AstronomicalPrayerCalculator.calculate(
            latitude = prefs.getLatitude(),
            longitude = prefs.getLongitude(),
            date = LocalDate.now(),
            zoneId = ZoneId.systemDefault(),
            method = prefs.getCalculationMethod(),
            school = prefs.getJuristicSchool(),
            fajrAdjustment = prefs.getPrayerAdjustment(PrayerType.FAJR),
            dhuhrAdjustment = prefs.getPrayerAdjustment(PrayerType.DHUHR),
            asrAdjustment = prefs.getPrayerAdjustment(PrayerType.ASR),
            maghribAdjustment = prefs.getPrayerAdjustment(PrayerType.MAGHRIB),
            ishaAdjustment = prefs.getPrayerAdjustment(PrayerType.ISHA),
            hijriDayOffset = prefs.getHijriOffset()
        )

        val now = LocalDateTime.now()

        schedule.prayers.forEachIndexed { index, prayer ->
            // Skip Sunrise for audio athan, or check if enabled
            if (prayer.type == PrayerType.SUNRISE || !prefs.isPrayerNotificationEnabled(prayer.type)) {
                return@forEachIndexed
            }

            var prayerTime = prayer.time
            if (prayerTime.isBefore(now)) {
                // Schedule for tomorrow if today's has passed
                prayerTime = prayerTime.plusDays(1)
            }

            val triggerMillis = prayerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER_ADHAN
                putExtra(EXTRA_PRAYER_NAME, prayer.type.displayNameAr)
                putExtra(EXTRA_IS_FAJR, prayer.type == PrayerType.FAJR)
            }

            val requestCode = 1000 + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled ${prayer.type} for $prayerTime")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
            }
        }
    }
}

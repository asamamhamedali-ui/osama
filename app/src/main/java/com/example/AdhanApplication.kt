package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.service.AdhanAlarmScheduler

class AdhanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Room DB
        AppDatabase.getDatabase(this)
        // Schedule exact alarms
        AdhanAlarmScheduler.scheduleDailyPrayers(this)
    }
}

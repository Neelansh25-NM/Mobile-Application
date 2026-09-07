package com.example.alarmboss

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.alarmboss.data.AlarmDatabase
import com.example.alarmboss.data.AlarmRepository

class AlarmBossApp : Application() {

    lateinit var repository: AlarmRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AlarmDatabase.getInstance(this)
        repository = AlarmRepository(db.alarmDao())
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm ringing notifications"
            setBypassDnd(true)
            enableVibration(true)
        }
        nm.createNotificationChannel(alarmChannel)
    }

    companion object {
        const val CHANNEL_ALARM = "alarm_channel"
    }
}

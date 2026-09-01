package com.example.alarmboss.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.example.alarmboss.AlarmBossApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Reschedules all enabled alarms after a device reboot (alarms don't survive reboot otherwise). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val app = context.applicationContext as AlarmBossApp
        val scheduler = AlarmScheduler(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.repository.getEnabledAlarms().forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

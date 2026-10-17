package com.example.alarmboss.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.alarmboss.data.Alarm
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return
        val triggerAtMillis = nextTriggerTime(alarm)
        val pendingIntent = pendingIntentFor(alarm.id)

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
                pendingIntent
            )
        } else {

            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /** Used for snooze: re-fires this same alarm id after N minutes, ignoring repeatDays. */
    fun scheduleOneOffInMinutes(alarm: Alarm, minutes: Int) {
        val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L
        val pendingIntent = pendingIntentFor(alarm.id)
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(alarmId: Long) {
        alarmManager.cancel(pendingIntentFor(alarmId))
    }

    fun scheduleWakeCheck(alarmId: Long, minutes: Int = 5) {
        val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WAKE_CHECK
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelWakeCheck(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WAKE_CHECK
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        cancelWakeCheckTimeout(alarmId)
    }

    /** Schedules fallback alarm ringing if user fails to confirm awake within seconds (default 60s). */
    fun scheduleWakeCheckTimeout(alarmId: Long, seconds: Int = 60) {
        val triggerAtMillis = System.currentTimeMillis() + seconds * 1000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WAKE_CHECK_TIMEOUT
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 300000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelWakeCheckTimeout(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WAKE_CHECK_TIMEOUT
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 300000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun pendingIntentFor(alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGER
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Computes the next epoch-millis trigger time, honoring repeatDays if set. */
    private fun nextTriggerTime(alarm: Alarm): Long {
        val now = LocalDateTime.now()
        var candidate = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0).withNano(0)

        if (alarm.repeatDays.isEmpty()) {
            if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
            return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        // Repeating alarm: find the next matching day-of-week (1..7) at/after now.
        for (offset in 0..7) {
            val day = candidate.plusDays(offset.toLong())
            val isoDay = day.dayOfWeek.value // 1=Mon..7=Sun
            val isToday = offset == 0
            val stillUpcomingToday = !day.isBefore(now)
            if (alarm.repeatDays.contains(isoDay) && (!isToday || stillUpcomingToday)) {
                return day.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
        // Fallback, shouldn't happen given the 8-day search window above.
        return candidate.plusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

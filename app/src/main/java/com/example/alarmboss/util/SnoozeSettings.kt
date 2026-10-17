package com.example.alarmboss.util

import android.content.Context

/** How long an Easy-mode alarm waits after Snooze. Stored in its own prefs file, apart from streaks. */
class SnoozeSettings(context: Context) {
    private val prefs = context.getSharedPreferences("alarm_boss_settings", Context.MODE_PRIVATE)

    var minutes: Int
        get() = sanitize(prefs.getInt(KEY_MINUTES, DEFAULT_MINUTES))
        set(value) {
            prefs.edit().putInt(KEY_MINUTES, sanitize(value)).apply()
        }

    companion object {
        private const val KEY_MINUTES = "snooze_minutes"
        const val DEFAULT_MINUTES = 5
        val OPTIONS = listOf(1, 5, 10, 15, 30)

        /** Anything outside the offered options (corrupt or hand-edited prefs) falls back to the default. */
        fun sanitize(value: Int): Int = if (value in OPTIONS) value else DEFAULT_MINUTES
    }
}

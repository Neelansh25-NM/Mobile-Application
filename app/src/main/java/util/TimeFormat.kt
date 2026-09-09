package com.example.alarmboss.util

import java.util.Locale

/** Formats a 24-hour (hour, minute) pair as a 12-hour clock string with AM/PM, e.g. "7:05 PM". */
fun formatTime12h(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, period)
}
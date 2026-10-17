package com.example.alarmboss.data

import java.util.Calendar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmLockTest {

    private fun at(hour: Int, minute: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    @Test
    fun `disabled alarms are never locked`() {
        assertFalse(isAlarmLocked(7, 0, isEnabled = false, now = at(6, 0)))
    }

    @Test
    fun `alarm one hour away is locked`() {
        assertTrue(isAlarmLocked(7, 0, isEnabled = true, now = at(6, 0)))
    }

    @Test
    fun `alarm three hours away is not locked`() {
        assertFalse(isAlarmLocked(9, 0, isEnabled = true, now = at(6, 0)))
    }

    @Test
    fun `alarm exactly at the two hour boundary is locked`() {
        assertTrue(isAlarmLocked(8, 0, isEnabled = true, now = at(6, 0)))
    }

    @Test
    fun `alarm time already passed today is treated as tomorrow and not locked`() {
        // 7:00 AM has already passed by 8:00 PM, so it rolls to tomorrow, well outside the window.
        assertFalse(isAlarmLocked(7, 0, isEnabled = true, now = at(20, 0)))
    }

    @Test
    fun `alarm just under two hours from firing tomorrow is locked`() {
        // 1:00 AM has passed by 11:15 PM, so it's locked for the 1:00 AM tomorrow occurrence.
        assertTrue(isAlarmLocked(1, 0, isEnabled = true, now = at(23, 15)))
    }
}

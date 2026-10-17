package com.example.alarmboss.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    @Test
    fun `midnight formats as 12 AM`() {
        assertEquals("12:00 AM", formatTime12h(0, 0))
    }

    @Test
    fun `noon formats as 12 PM`() {
        assertEquals("12:00 PM", formatTime12h(12, 0))
    }

    @Test
    fun `morning hour formats with AM and zero-padded minutes`() {
        assertEquals("7:05 AM", formatTime12h(7, 5))
    }

    @Test
    fun `afternoon hour converts from 24-hour to 12-hour with PM`() {
        assertEquals("6:30 PM", formatTime12h(18, 30))
    }

    @Test
    fun `hour just before midnight formats as 11 PM`() {
        assertEquals("11:59 PM", formatTime12h(23, 59))
    }
}

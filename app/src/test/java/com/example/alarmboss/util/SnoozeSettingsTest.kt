package com.example.alarmboss.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnoozeSettingsTest {

    @Test
    fun `every offered option is kept as is`() {
        SnoozeSettings.OPTIONS.forEach { assertEquals(it, SnoozeSettings.sanitize(it)) }
    }

    @Test
    fun `values outside the options fall back to the default`() {
        assertEquals(SnoozeSettings.DEFAULT_MINUTES, SnoozeSettings.sanitize(0))
        assertEquals(SnoozeSettings.DEFAULT_MINUTES, SnoozeSettings.sanitize(-5))
        assertEquals(SnoozeSettings.DEFAULT_MINUTES, SnoozeSettings.sanitize(7))
        assertEquals(SnoozeSettings.DEFAULT_MINUTES, SnoozeSettings.sanitize(600))
    }

    @Test
    fun `the default is one of the offered options`() {
        assertTrue(SnoozeSettings.DEFAULT_MINUTES in SnoozeSettings.OPTIONS)
    }
}

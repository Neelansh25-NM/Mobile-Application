package com.example.alarmboss.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsParsingTest {

    @Test
    fun `passages are split on dashes and trimmed`() {
        assertEquals(listOf("first one", "second one"), parsePassages("  first one \n---\n second one  "))
    }

    @Test
    fun `blank passages are dropped`() {
        assertEquals(listOf("only"), parsePassages("---\n  \n---only---\n---"))
    }

    @Test
    fun `empty input gives no passages`() {
        assertTrue(parsePassages("").isEmpty())
    }

    @Test
    fun `passages survive a format and parse round trip`() {
        val passages = listOf("alpha beta", "gamma\ndelta")
        assertEquals(passages, parsePassages(formatPassages(passages)))
    }

    @Test
    fun `lyrics are one per line with blanks removed`() {
        assertEquals(listOf("line one", "line two"), parseLyrics("line one\n\n  line two  \n"))
    }

    @Test
    fun `lyrics survive a format and parse round trip`() {
        val lines = listOf("one", "two", "three")
        assertEquals(lines, parseLyrics(formatLyrics(lines)))
    }
}

package com.example.alarmboss.util

import org.junit.Assert.assertEquals
import org.junit.Test

class WordOverlapTest {

    @Test
    fun `identical text is a full match`() {
        assertEquals(100, wordOverlapPercent("the quick brown fox", "the quick brown fox"))
    }

    @Test
    fun `comparison is case-insensitive`() {
        assertEquals(100, wordOverlapPercent("Hello World", "hello world"))
    }

    @Test
    fun `partial overlap is scored proportionally to the target`() {
        assertEquals(50, wordOverlapPercent("one two three four", "one two"))
    }

    @Test
    fun `completely different text scores zero`() {
        assertEquals(0, wordOverlapPercent("apples and oranges", "completely unrelated words"))
    }

    @Test
    fun `blank target scores zero rather than dividing by zero`() {
        assertEquals(0, wordOverlapPercent("", "anything"))
    }

    @Test
    fun `punctuation does not prevent a match`() {
        assertEquals(100, wordOverlapPercent("hello, world!", "hello world"))
    }
}

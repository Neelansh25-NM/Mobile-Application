package com.example.alarmboss.ui.stopwatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StopwatchStateTest {

    @Test
    fun `a fresh stopwatch is stopped at zero`() {
        val state = StopwatchState()
        assertFalse(state.running)
        assertEquals(0L, state.elapsed(now = 5_000))
    }

    @Test
    fun `elapsed time follows the clock while running`() {
        val state = StopwatchState().start(now = 1_000)
        assertTrue(state.running)
        assertEquals(2_500L, state.elapsed(now = 3_500))
    }

    @Test
    fun `pausing freezes the elapsed time`() {
        val paused = StopwatchState().start(now = 1_000).pause(now = 4_000)
        assertFalse(paused.running)
        assertEquals(3_000L, paused.elapsed(now = 99_000))
    }

    @Test
    fun `resuming continues from the paused time`() {
        val resumed = StopwatchState().start(now = 0).pause(now = 3_000).start(now = 10_000)
        assertEquals(5_000L, resumed.elapsed(now = 12_000))
    }

    @Test
    fun `starting an already running stopwatch changes nothing`() {
        val running = StopwatchState().start(now = 1_000)
        assertEquals(running, running.start(now = 9_000))
    }

    @Test
    fun `pausing a stopped stopwatch changes nothing`() {
        val stopped = StopwatchState(accumulatedMs = 700)
        assertEquals(stopped, stopped.pause(now = 9_000))
    }

    @Test
    fun `reset returns to zero and stopped`() {
        assertEquals(StopwatchState(), StopwatchState().start(now = 0).reset())
    }

    @Test
    fun `a clock reading before the start never produces negative time`() {
        assertEquals(0L, StopwatchState().start(now = 5_000).elapsed(now = 4_000))
    }
}

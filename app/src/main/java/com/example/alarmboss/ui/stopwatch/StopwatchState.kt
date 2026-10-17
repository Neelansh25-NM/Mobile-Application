package com.example.alarmboss.ui.stopwatch

/**
 * Pure stopwatch model. Elapsed time is derived from a start timestamp rather than counted
 * frame by frame, so it stays correct while the screen isn't composed, across rotation, and
 * after the process is recreated. [runStartedAt] must come from a monotonic clock
 * (SystemClock.elapsedRealtime), which keeps counting while the app is in the background.
 */
data class StopwatchState(
    val accumulatedMs: Long = 0L,
    val runStartedAt: Long? = null
) {
    val running: Boolean get() = runStartedAt != null

    fun elapsed(now: Long): Long = accumulatedMs + (runStartedAt?.let { (now - it).coerceAtLeast(0L) } ?: 0L)

    fun start(now: Long): StopwatchState = if (running) this else copy(runStartedAt = now)

    fun pause(now: Long): StopwatchState =
        if (running) StopwatchState(accumulatedMs = elapsed(now), runStartedAt = null) else this

    fun reset(): StopwatchState = StopwatchState()
}

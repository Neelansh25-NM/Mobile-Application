package com.example.alarmboss.ui.stopwatch

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * Held at activity scope so the stopwatch survives rotation and switching bottom-bar tabs, and
 * mirrored into [SavedStateHandle] so it also survives the system recreating the process.
 */
class StopwatchViewModel(private val handle: SavedStateHandle) : ViewModel() {

    var state by mutableStateOf(
        StopwatchState(
            accumulatedMs = handle[KEY_ACCUMULATED] ?: 0L,
            runStartedAt = handle.get<Long>(KEY_STARTED_AT)?.takeIf { it >= 0L }
        )
    )
        private set

    // Newest lap first, matching how the screen lists them.
    val laps = mutableStateListOf<Long>().apply {
        handle.get<LongArray>(KEY_LAPS)?.let { addAll(it.toList()) }
    }

    fun toggle() {
        val now = SystemClock.elapsedRealtime()
        update(if (state.running) state.pause(now) else state.start(now))
    }

    fun lap() {
        if (!state.running) return
        laps.add(0, state.elapsed(SystemClock.elapsedRealtime()))
        handle[KEY_LAPS] = laps.toLongArray()
    }

    fun reset() {
        update(state.reset())
        laps.clear()
        handle[KEY_LAPS] = LongArray(0)
    }

    private fun update(next: StopwatchState) {
        state = next
        handle[KEY_ACCUMULATED] = next.accumulatedMs
        handle[KEY_STARTED_AT] = next.runStartedAt ?: -1L
    }

    private companion object {
        const val KEY_ACCUMULATED = "stopwatch_accumulated"
        const val KEY_STARTED_AT = "stopwatch_started_at"
        const val KEY_LAPS = "stopwatch_laps"
    }
}

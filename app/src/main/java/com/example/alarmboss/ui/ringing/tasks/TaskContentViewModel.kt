package com.example.alarmboss.ui.ringing.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmboss.data.ContentStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** The lyric line for the sing-along task, with "still loading" kept distinct from "none set up". */
sealed interface LyricState {
    data object Loading : LyricState
    data object NoLines : LyricState
    data class Line(val text: String) : LyricState
}

/**
 * Supplies the user's own reading passage and lyric line to the Medium tasks. The choice is made
 * once and held here, so it stays the same if the screen is recreated instead of re-rolling.
 */
class TaskContentViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ContentStore(application)

    private val _passage = MutableStateFlow<String?>(null)
    val passage: StateFlow<String?> = _passage

    private val _lyric = MutableStateFlow<LyricState>(LyricState.Loading)
    val lyric: StateFlow<LyricState> = _lyric

    fun loadPassage() {
        if (_passage.value != null) return
        viewModelScope.launch { _passage.value = store.getRandomPassage() }
    }

    fun loadLyric() {
        if (_lyric.value !is LyricState.Loading) return
        viewModelScope.launch {
            _lyric.value = store.getRandomLyric()?.let { LyricState.Line(it) } ?: LyricState.NoLines
        }
    }
}

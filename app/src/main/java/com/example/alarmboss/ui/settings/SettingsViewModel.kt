package com.example.alarmboss.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmboss.data.ContentStore
import com.example.alarmboss.util.SnoozeSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Reading passages are separated by a line containing three dashes, one passage per block. */
internal fun parsePassages(text: String): List<String> =
    text.split("---").map { it.trim() }.filter { it.isNotBlank() }

internal fun formatPassages(passages: List<String>): String = passages.joinToString("\n---\n")

/** Lyric lines are one per line; blank lines are ignored. */
internal fun parseLyrics(text: String): List<String> =
    text.split("\n").map { it.trim() }.filter { it.isNotBlank() }

internal fun formatLyrics(lines: List<String>): String = lines.joinToString("\n")

/**
 * Holds the editable drafts for the Settings screen. Drafts live here, not in the composable, so
 * typing survives rotation, and the stores are only touched when the user saves.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ContentStore(application)
    private val snoozeSettings = SnoozeSettings(application)

    private val _snoozeMinutes = MutableStateFlow(snoozeSettings.minutes)
    val snoozeMinutes: StateFlow<Int> = _snoozeMinutes

    private val _passagesText = MutableStateFlow("")
    val passagesText: StateFlow<String> = _passagesText

    private val _lyricsText = MutableStateFlow("")
    val lyricsText: StateFlow<String> = _lyricsText

    private val counselorEngine = com.example.alarmboss.counselor.SleepCounselorEngine(application)
    private val _isGeneratingPassages = MutableStateFlow(false)
    val isGeneratingPassages: StateFlow<Boolean> = _isGeneratingPassages

    private val _barcodeText = MutableStateFlow("")
    val barcodeText: StateFlow<String> = _barcodeText

    init {
        // Seed the drafts once from what's stored. Reading continuously would overwrite
        // whatever the user is in the middle of typing.
        viewModelScope.launch {
            _passagesText.value = formatPassages(store.readingPassages.first())
            _lyricsText.value = formatLyrics(store.lyricLines.first())
            _barcodeText.value = store.getRegisteredBarcode() ?: ""
        }
    }

    fun setSnoozeMinutes(minutes: Int) {
        snoozeSettings.minutes = minutes
        _snoozeMinutes.value = snoozeSettings.minutes
    }

    fun onPassagesChange(text: String) { _passagesText.value = text }
    fun onLyricsChange(text: String) { _lyricsText.value = text }
    fun onBarcodeChange(text: String) { _barcodeText.value = text }

    fun savePassages() {
        val passages = parsePassages(_passagesText.value)
        viewModelScope.launch { store.setReadingPassages(passages) }
    }

    fun saveLyrics() {
        val lines = parseLyrics(_lyricsText.value)
        viewModelScope.launch { store.setLyricLines(lines) }
    }

    fun saveBarcode() {
        viewModelScope.launch { store.setRegisteredBarcode(_barcodeText.value) }
    }

    fun generatePassagesWithGemini() {
        if (_isGeneratingPassages.value) return
        viewModelScope.launch {
            _isGeneratingPassages.value = true
            try {
                val generated = counselorEngine.generateReadingPassages()
                _passagesText.value = generated
                savePassages()
            } finally {
                _isGeneratingPassages.value = false
            }
        }
    }
}

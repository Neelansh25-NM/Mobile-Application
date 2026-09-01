package com.example.alarmboss.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "alarmboss_content")

/**
 * Holds user-supplied content for Medium-mode tasks. We deliberately do NOT ship any
 * copyrighted song lyrics or third-party text with the app -- the user types/pastes
 * their own reading passages and lyric lines here, and sets which physical barcode/QR
 * code (e.g. one they stick on the bathroom mirror) the barcode task must scan.
 */
class ContentStore(private val context: Context) {

    private object Keys {
        val READING_PASSAGES = stringSetPreferencesKey("reading_passages")
        val LYRIC_LINES = stringSetPreferencesKey("lyric_lines")
        val BARCODE_TARGET = stringPreferencesKey("barcode_target")
    }

    val readingPassages: Flow<List<String>> =
        context.dataStore.data.map { it[Keys.READING_PASSAGES]?.toList() ?: defaultPassages }

    val lyricLines: Flow<List<String>> =
        context.dataStore.data.map { it[Keys.LYRIC_LINES]?.toList() ?: emptyList() }

    val barcodeTarget: Flow<String?> =
        context.dataStore.data.map { it[Keys.BARCODE_TARGET] }

    suspend fun setReadingPassages(passages: List<String>) {
        context.dataStore.edit { it[Keys.READING_PASSAGES] = passages.toSet() }
    }

    suspend fun setLyricLines(lines: List<String>) {
        context.dataStore.edit { it[Keys.LYRIC_LINES] = lines.toSet() }
    }

    suspend fun setBarcodeTarget(value: String) {
        context.dataStore.edit { it[Keys.BARCODE_TARGET] = value }
    }

    suspend fun getRandomPassage(): String = readingPassages.first().randomOrNull() ?: defaultPassages.first()
    suspend fun getRandomLyric(): String? = lyricLines.first().randomOrNull()

    companion object {
        // Generic, non-copyrighted filler text used only if the user hasn't added their own yet.
        val defaultPassages = listOf(
            "Add your own reading passages in Settings so this task quizzes you on text " +
                "you actually chose. Until then, here is a placeholder paragraph: the quick " +
                "brown fox jumps over the lazy dog while the early morning sun rises steadily " +
                "over the quiet hills, and a gentle breeze moves through the trees."
        )
    }
}

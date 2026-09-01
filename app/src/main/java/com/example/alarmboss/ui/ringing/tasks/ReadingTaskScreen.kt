package com.example.alarmboss.ui.ringing.tasks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.alarmboss.data.ContentStore
import com.example.alarmboss.util.wordOverlapPercent
import java.util.Locale

/**
 * Shows a passage the user configured in Settings. "Done" only unlocks once they've
 * scrolled to the bottom AND read the last sentence aloud (checked via on-device speech
 * recognition against a word-overlap threshold) -- or typed it, as a fallback for devices
 * where speech recognition isn't available/reliable (common on some OEM Android skins).
 */
@Composable
fun ReadingTaskScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ContentStore(context) }
    var passage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    var heard by remember { mutableStateOf("") }
    var typed by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var recognitionError by remember { mutableStateOf<String?>(null) }
    val recognitionAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    LaunchedEffect(Unit) { passage = store.getRandomPassage() }

    // Only the closing sentence needs to be read aloud -- speech recognition sessions are
    // short-lived, so checking the whole passage isn't reliable, but the last sentence is
    // enough to confirm they actually reached the end and read it.
    val confirmPhrase = remember(passage) {
        passage?.trim()?.split(Regex("\\s+"))?.takeLast(8)?.joinToString(" ") ?: ""
    }
    val scrolledToEnd = scrollState.value >= (scrollState.maxValue - 4).coerceAtLeast(0)
    val bestMatch = maxOf(
        wordOverlapPercent(confirmPhrase, heard),
        wordOverlapPercent(confirmPhrase, typed)
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            listening = true
            startReadingListener(context, onError = { recognitionError = it; listening = false }) { r ->
                heard = r; listening = false
            }
        } else {
            listening = false
        }
    }

    fun startListening() {
        recognitionError = null
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        listening = true
        startReadingListener(context, onError = { recognitionError = it; listening = false }) { r ->
            heard = r
            listening = false
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Read the passage below, then read the last line aloud", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(scrollState)) {
            Text(passage ?: "Loading…")
        }
        Spacer(Modifier.height(12.dp))
        if (scrolledToEnd) {
            Text("Read this line aloud to confirm:", style = MaterialTheme.typography.bodySmall)
            Text("\"$confirmPhrase\"", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))

            if (!recognitionAvailable) {
                Text(
                    "Voice recognition isn't available on this device. Type the line instead:",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                if (heard.isNotBlank()) {
                    Text("Heard: \"$heard\"", style = MaterialTheme.typography.bodySmall)
                }
                if (recognitionError != null) {
                    Text(
                        "Couldn't hear that ($recognitionError). Try again or type below.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(onClick = { startListening() }, enabled = !listening) {
                    Text(if (listening) "Listening…" else "Tap and read aloud")
                }
                Spacer(Modifier.height(8.dp))
                Text("or type it:", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(value = typed, onValueChange = { typed = it }, label = { Text("Typed answer") })
            Spacer(Modifier.height(8.dp))
            Text("Match: $bestMatch%", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onDone, enabled = bestMatch >= 60) { Text("Done") }
        } else {
            Text("Scroll to the bottom to continue", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun startReadingListener(
    context: android.content.Context,
    onError: (String) -> Unit = {},
    onResult: (String) -> Unit
) {
    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    }
    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onResults(results: android.os.Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            onResult(matches?.firstOrNull() ?: "")
            recognizer.destroy()
        }
        override fun onError(error: Int) {
            val name = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "no match"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "timed out"
                SpeechRecognizer.ERROR_NETWORK -> "network error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "missing permission"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "recognizer busy"
                else -> "error $error"
            }
            onError(name)
            recognizer.destroy()
        }
        override fun onReadyForSpeech(params: android.os.Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: android.os.Bundle?) {}
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    })
    recognizer.startListening(intent)
}
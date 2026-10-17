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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmboss.R
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
    val contentViewModel: TaskContentViewModel = viewModel()
    val passage by contentViewModel.passage.collectAsState()
    val scrollState = rememberScrollState()
    var heard by remember { mutableStateOf("") }
    var typed by remember { mutableStateOf("") }
    var isTypingMode by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var recognitionError by remember { mutableStateOf<String?>(null) }
    val recognitionAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    LaunchedEffect(Unit) { contentViewModel.loadPassage() }

    // Only the closing sentence needs to be read aloud -- speech recognition sessions are
    // short-lived, so checking the whole passage isn't reliable, but the last sentence is
    // enough to confirm they actually reached the end and read it.
    val confirmPhrase = remember(passage) {
        passage?.trim()?.split(Regex("\\s+"))?.takeLast(8)?.joinToString(" ") ?: ""
    }
    val scrolledToEnd = scrollState.value >= (scrollState.maxValue - 4).coerceAtLeast(0)

    // Calculate voice match percentage based on the threshold
    val voiceMatchPercent = wordOverlapPercent(confirmPhrase, heard)

    // Enforce strict, case-sensitive exact match for typing
    val isTypedCorrectly = typed.trim() == confirmPhrase.trim()

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
        Text(stringResource(R.string.reading_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(scrollState)) {
            Text(passage ?: stringResource(R.string.reading_loading))
        }
        Spacer(Modifier.height(12.dp))
        if (scrolledToEnd) {
            if (isTypingMode) {
                // --- TYPING MODE UI ---
                Text(stringResource(R.string.reading_type_prompt), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.task_quoted, confirmPhrase), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    label = { Text(stringResource(R.string.task_typed_answer)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (typed.isNotBlank() && !isTypedCorrectly) {
                    Text(
                        stringResource(R.string.reading_type_mismatch),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (isTypedCorrectly) {
                    Text(
                        stringResource(R.string.reading_exact_match),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { isTypingMode = false }) {
                    Text(stringResource(R.string.reading_switch_to_voice))
                }

            } else {
                // --- VOICE DICTATION MODE UI ---
                Text(stringResource(R.string.reading_read_prompt), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.task_quoted, confirmPhrase), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))

                if (!recognitionAvailable) {
                    Text(
                        stringResource(R.string.task_voice_unavailable),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { isTypingMode = true }) {
                        Text(stringResource(R.string.reading_switch_to_typing_mode))
                    }
                } else {
                    if (heard.isNotBlank()) {
                        Text(stringResource(R.string.task_heard, heard), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.task_voice_match_percent, voiceMatchPercent), style = MaterialTheme.typography.bodySmall)
                    }
                    if (recognitionError != null) {
                        Text(
                            stringResource(R.string.reading_error, recognitionError.orEmpty()),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { startListening() }, enabled = !listening) {
                        Text(stringResource(if (listening) R.string.task_listening else R.string.reading_tap_to_read))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { isTypingMode = true }) {
                        Text(stringResource(R.string.reading_switch_to_typing))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- DONE BUTTON VALIDATION ---
            Button(
                onClick = onDone,
                enabled = if (isTypingMode) isTypedCorrectly else voiceMatchPercent >= 60
            ) {
                Text(stringResource(R.string.task_done))
            }
        } else {
            Text(stringResource(R.string.reading_scroll_hint), style = MaterialTheme.typography.bodySmall)
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
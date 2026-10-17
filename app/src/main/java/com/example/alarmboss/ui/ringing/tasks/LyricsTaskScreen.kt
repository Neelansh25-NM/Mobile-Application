package com.example.alarmboss.ui.ringing.tasks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmboss.R
import com.example.alarmboss.util.wordOverlapPercent
import java.util.Locale

/**
 * "Sing along" task. Honest limitation: we can't judge singing quality or pitch, only
 * whether the words spoken/sung match the lyric line closely enough via on-device speech
 * recognition. Requires the user to add their OWN lyric lines in Settings (no copyrighted
 * lyrics are shipped). Also includes a manual "type what you sang" fallback, since on
 * several Android OEM skins (notably MIUI/HyperOS devices without Google set as the
 * default assistant/voice-input app) on-device SpeechRecognizer silently returns nothing
 * rather than erroring -- the typed fallback keeps the task usable on those devices.
 */
@Composable
fun LyricsTaskScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val contentViewModel: TaskContentViewModel = viewModel()
    val lyricState by contentViewModel.lyric.collectAsState()
    val line = (lyricState as? LyricState.Line)?.text
    var heard by remember { mutableStateOf("") }
    var typed by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var recognitionError by remember { mutableStateOf<String?>(null) }
    val recognitionAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    LaunchedEffect(Unit) { contentViewModel.loadLyric() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            listening = true
            startListeningInternal(context, onError = { recognitionError = it; listening = false }) { r ->
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
        startListeningInternal(context, onError = { recognitionError = it; listening = false }) { r ->
            heard = r
            listening = false
        }
    }

    val bestMatch = maxOf(
        wordOverlapPercent(line.orEmpty(), heard),
        wordOverlapPercent(line.orEmpty(), typed)
    )

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (lyricState is LyricState.Loading) {
            // Don't offer the "no lyrics, continue" skip while the line is still being fetched.
            CircularProgressIndicator()
        } else if (line == null) {
            Text(
                stringResource(R.string.lyrics_none),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDone) { Text(stringResource(R.string.task_continue)) }
        } else {
            Text(stringResource(R.string.lyrics_sing_this), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.task_quoted, line.orEmpty()), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            if (!recognitionAvailable) {
                Text(
                    stringResource(R.string.lyrics_no_voice),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                if (heard.isNotBlank()) {
                    Text(stringResource(R.string.task_heard, heard), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }
                if (recognitionError != null) {
                    Text(
                        stringResource(R.string.lyrics_error, recognitionError.orEmpty()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(onClick = { startListening() }, enabled = !listening) {
                    Text(stringResource(if (listening) R.string.task_listening else R.string.lyrics_tap_to_sing))
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.lyrics_or_type), style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = typed,
                onValueChange = { typed = it },
                label = { Text(stringResource(R.string.task_typed_answer)) }
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.task_match_percent, bestMatch), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDone, enabled = bestMatch >= 60) { Text(stringResource(R.string.task_done)) }
        }
    }
}

private fun startListeningInternal(
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
            onError(speechErrorName(error))
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

private fun speechErrorName(error: Int): String = when (error) {
    SpeechRecognizer.ERROR_NO_MATCH -> "no match"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "timed out"
    SpeechRecognizer.ERROR_NETWORK -> "network error"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "missing permission"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "recognizer busy"
    else -> "error $error"
}
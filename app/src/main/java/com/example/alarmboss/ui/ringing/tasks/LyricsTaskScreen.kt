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
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.alarmboss.data.ContentStore
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * "Sing along" task. Honest limitation: we can't judge singing quality or pitch, only
 * whether the words spoken/sung match the lyric line closely enough via on-device speech
 * recognition. It requires the user to add their OWN lyric lines in Settings (the app
 * ships no copyrighted lyrics) and checks word overlap against what SpeechRecognizer heard.
 */
@Composable
fun LyricsTaskScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ContentStore(context) }
    var line by remember { mutableStateOf<String?>(null) }
    var heard by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var matchPercent by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { line = store.getRandomLyric() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) listening = true }

    fun startListening() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: android.os.Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                heard = matches?.firstOrNull() ?: ""
                matchPercent = wordOverlapPercent(line.orEmpty(), heard)
                listening = false
                recognizer.destroy()
            }
            override fun onError(error: Int) { listening = false; recognizer.destroy() }
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

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (line == null) {
            Text(
                "No lyric lines set up yet. Add some in Settings, then this task will " +
                    "ask you to sing them. Skipping for now.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDone) { Text("Continue") }
        } else {
            Text("Sing this line:", style = MaterialTheme.typography.titleMedium)
            Text("\"$line\"", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            if (heard.isNotBlank()) {
                Text("Heard: \"$heard\" ($matchPercent% match)", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            Button(onClick = { startListening() }, enabled = !listening) {
                Text(if (listening) "Listening…" else "Tap and sing")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDone, enabled = matchPercent >= 60) { Text("Done") }
        }
    }
}

private fun wordOverlapPercent(target: String, heard: String): Int {
    val targetWords = target.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
    val heardWords = heard.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
    if (targetWords.isEmpty()) return 0
    val overlap = targetWords.intersect(heardWords).size
    return (overlap * 100) / targetWords.size
}

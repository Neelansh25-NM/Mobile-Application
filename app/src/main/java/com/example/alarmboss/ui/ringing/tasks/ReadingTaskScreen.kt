package com.example.alarmboss.ui.ringing.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.alarmboss.data.ContentStore
import kotlinx.coroutines.launch

/**
 * Shows a passage the user configured in Settings. The "Done" button only unlocks once
 * they've scrolled to the bottom AND retyped a short confirmation phrase pulled from the
 * end of the passage -- a lightweight anti-cheat so they can't dismiss without reading.
 */
@Composable
fun ReadingTaskScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ContentStore(context) }
    var passage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    var confirmText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        passage = store.getRandomPassage()
    }

    val confirmPhrase = remember(passage) {
        passage?.trim()?.split(Regex("\\s+"))?.takeLast(4)?.joinToString(" ") ?: ""
    }
    val scrolledToEnd = scrollState.value >= (scrollState.maxValue - 4).coerceAtLeast(0)
    val typedCorrectly = confirmText.trim().equals(confirmPhrase.trim(), ignoreCase = true)

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Read the passage below, then confirm", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(scrollState)) {
            Text(passage ?: "Loading…")
        }
        Spacer(Modifier.height(12.dp))
        if (scrolledToEnd) {
            Text("Type the last few words to confirm you read it:", style = MaterialTheme.typography.bodySmall)
            Text("\"$confirmPhrase\"", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = confirmText, onValueChange = { confirmText = it })
            Spacer(Modifier.height(8.dp))
            Button(onClick = onDone, enabled = typedCorrectly) { Text("Done") }
        } else {
            Text("Scroll to the bottom to continue", style = MaterialTheme.typography.bodySmall)
        }
    }
}

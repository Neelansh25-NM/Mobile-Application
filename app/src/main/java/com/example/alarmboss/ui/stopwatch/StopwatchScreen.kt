package com.example.alarmboss.ui.stopwatch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen() {
    var running by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var startBase by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        startBase = System.currentTimeMillis() - elapsedMs
        while (running) {
            elapsedMs = System.currentTimeMillis() - startBase
            delay(31) // ~30fps refresh, plenty for a stopwatch display
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Stopwatch") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(formatMillis(elapsedMs), style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { running = !running }) { Text(if (running) "Pause" else "Start") }
                OutlinedButton(onClick = { if (running) laps.add(0, elapsedMs) }) { Text("Lap") }
                OutlinedButton(onClick = { running = false; elapsedMs = 0L; laps.clear() }) { Text("Reset") }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(Modifier.fillMaxWidth()) {
                items(laps.size) { index ->
                    val lapNumber = laps.size - index
                    Text("Lap $lapNumber: ${formatMillis(laps[index])}", modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val minutes = (ms / 60000)
    val seconds = (ms / 1000) % 60
    val centis = (ms % 1000) / 10
    return String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, centis)
}

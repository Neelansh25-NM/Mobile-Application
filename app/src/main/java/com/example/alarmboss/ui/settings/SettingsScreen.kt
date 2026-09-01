package com.example.alarmboss.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.alarmboss.data.ContentStore
import kotlinx.coroutines.launch

/**
 * Where the user supplies their own content for Medium-mode tasks: reading passages and
 * lyric lines (we never ship copyrighted text), plus the target barcode/QR value for the
 * barcode task. One passage/line per row of the text box.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val store = remember { ContentStore(context) }
    val scope = rememberCoroutineScope()

    var passagesText by remember { mutableStateOf("") }
    var lyricsText by remember { mutableStateOf("") }
    var barcodeTarget by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        store.readingPassages.collect { passagesText = it.joinToString("\n---\n") }
    }
    LaunchedEffect(Unit) {
        store.lyricLines.collect { lyricsText = it.joinToString("\n") }
    }
    LaunchedEffect(Unit) {
        store.barcodeTarget.collect { barcodeTarget = it ?: "" }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Task content") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Add your own content below so Medium-mode tasks quiz you on things you " +
                    "chose (we don't ship any copyrighted text or lyrics).",
                style = MaterialTheme.typography.bodySmall
            )

            Text("Reading passages (separate with a line containing ---)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = passagesText, onValueChange = { passagesText = it },
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Button(onClick = {
                scope.launch { store.setReadingPassages(passagesText.split("---").map { it.trim() }.filter { it.isNotBlank() }) }
            }) { Text("Save passages") }

            Text("Lyric lines (one per line)", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = lyricsText, onValueChange = { lyricsText = it },
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Button(onClick = {
                scope.launch { store.setLyricLines(lyricsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }) }
            }) { Text("Save lyrics") }

            Text("Barcode task target value", style = MaterialTheme.typography.titleSmall)
            Text(
                "Scan a barcode/QR code with any scanner app to get its text value, or just " +
                    "type something unique and print/write it as a QR code to stick somewhere " +
                    "you must get up to reach.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(value = barcodeTarget, onValueChange = { barcodeTarget = it }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { scope.launch { store.setBarcodeTarget(barcodeTarget) } }) { Text("Save barcode target") }
        }
    }
}

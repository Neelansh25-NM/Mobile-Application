package com.example.alarmboss.ui.alarms

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Launches the system document picker filtered to audio files and persists a long-term
 * read permission on the chosen URI so it survives reboots (needed since AlarmService
 * plays the sound long after the picker closes).
 */
@Composable
fun rememberSoundPickerLauncher(onPicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permissions; the sound will
                // still work until the app/device restarts in that edge case.
            }
            onPicked(uri.toString())
        }
    }
    return { launcher.launch(arrayOf("audio/*")) }
}

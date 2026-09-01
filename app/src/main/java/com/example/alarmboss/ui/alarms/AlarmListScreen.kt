package com.example.alarmboss.ui.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmboss.data.Alarm
import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.util.formatTime12h
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    viewModel: AlarmViewModel = viewModel()
) {
    val alarms by viewModel.alarms.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Alarms") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlarm) { Icon(Icons.Default.Add, contentDescription = "Add alarm") }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No alarms yet. Tap + to add one.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmRow(
                        alarm = alarm,
                        onToggle = { enabled -> viewModel.setEnabled(alarm, enabled) },
                        onClick = { onEditAlarm(alarm.id) },
                        onDelete = { viewModel.delete(alarm) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .clickable(onClick = onClick)
        ) {
            Text(
                formatTime12h(alarm.hour, alarm.minute),
                style = MaterialTheme.typography.headlineMedium
            )
            if (alarm.label.isNotBlank()) Text(alarm.label, style = MaterialTheme.typography.bodyMedium)
            Text(modeLabel(alarm.mode), style = MaterialTheme.typography.bodySmall)
            if (alarm.repeatDays.isNotEmpty()) {
                Text(daysLabel(alarm.repeatDays), style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        Switch(checked = alarm.isEnabled, onCheckedChange = onToggle)
    }
}

private fun modeLabel(mode: AlarmMode) = when (mode) {
    AlarmMode.EASY -> "Easy"
    AlarmMode.MEDIUM -> "Medium · random task"
    AlarmMode.STRICT -> "Strict · exercise"
}

private fun daysLabel(days: Set<Int>): String =
    days.sorted().joinToString(", ") {
        DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
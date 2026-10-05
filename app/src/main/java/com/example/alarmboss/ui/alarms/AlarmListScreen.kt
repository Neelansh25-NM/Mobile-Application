package com.example.alarmboss.ui.alarms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmboss.data.Alarm
import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.ExerciseType
import com.example.alarmboss.data.MentalExerciseType
import com.example.alarmboss.data.StrictExerciseCategory
import com.example.alarmboss.data.isAlarmLocked
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
    val currentStreak by viewModel.streak.collectAsState(initial = 0)

    // Reload the streak whenever the screen is composed or resumed
    LaunchedEffect(Unit) {
        viewModel.loadStreak()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Alarms") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlarm) {
                Icon(Icons.Default.Add, contentDescription = "Add alarm")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Wake-Up Streak UI
            if (currentStreak > 0) {
                // Wake-Up Streak UI - Always visible
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentStreak > 0) "🔥 Current Streak: $currentStreak Days 🔥" else "🔥 Current Streak: 0 Days (Wake up tomorrow to start!) 🔥",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFA500)
                    )
                }
            }

            if (alarms.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No alarms yet. Tap + to add one.")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
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
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isLocked = isAlarmLocked(alarm.hour, alarm.minute, alarm.isEnabled)

    Row(
        Modifier
            .fillMaxWidth()
            .then(if (isLocked) Modifier.background(Color.LightGray.copy(alpha = 0.3f)) else Modifier)
            .clickable(enabled = !isLocked, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.weight(1f)
        ) {
            Text(
                formatTime12h(alarm.hour, alarm.minute),
                style = MaterialTheme.typography.headlineMedium,
                color = if (isLocked) Color.Gray else Color.Unspecified
            )
            if (alarm.label.isNotBlank()) Text(alarm.label, style = MaterialTheme.typography.bodyMedium)
            Text(modeLabel(alarm), style = MaterialTheme.typography.bodySmall)
            if (alarm.repeatDays.isNotEmpty()) {
                Text(daysLabel(alarm.repeatDays), style = MaterialTheme.typography.bodySmall)
            }
        }

        if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.Red,
                modifier = Modifier.padding(12.dp)
            )
        } else {
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }

        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = onToggle,
            enabled = !isLocked
        )
    }
}

private fun modeLabel(alarm: Alarm): String = when (alarm.mode) {
    AlarmMode.EASY -> "Easy"
    AlarmMode.MEDIUM -> "Medium · random task"
    AlarmMode.STRICT -> {
        if (alarm.strictCategory == StrictExerciseCategory.PHYSICAL) {
            val exName = if (alarm.exerciseType == ExerciseType.SQUATS) "Squats" else "Jumping jacks"
            "Strict · Physical ($exName)"
        } else {
            val mentalName = if (alarm.mentalExerciseType == MentalExerciseType.MEMORY_GRID) "Memory Grid" else "Maze"
            "Strict · Mental ($mentalName)"
        }
    }
}

private fun daysLabel(days: Set<Int>): String =
    days.sorted().joinToString(", ") {
        DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
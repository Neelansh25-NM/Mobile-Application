package com.example.alarmboss.ui.alarms

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmboss.data.*
import com.example.alarmboss.util.formatTime12h
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long?,
    onDone: () -> Unit,
    viewModel: AlarmViewModel = viewModel()
) {
    val context = LocalContext.current
    var loaded by remember { mutableStateOf(alarmId == null) }

    LaunchedEffect(alarmId) {
        if (alarmId != null) {
            // AlarmViewModel exposes a StateFlow of all alarms; find the matching one.
        }
        loaded = true
    }

    val alarms by viewModel.alarms.collectAsState()
    val base = remember(alarms, alarmId) { alarms.find { it.id == alarmId } }

    var hour by remember(base) { mutableIntStateOf(base?.hour ?: 7) }
    var minute by remember(base) { mutableIntStateOf(base?.minute ?: 0) }
    var label by remember(base) { mutableStateOf(base?.label ?: "") }
    var mode by remember(base) { mutableStateOf(base?.mode ?: AlarmMode.EASY) }
    var days by remember(base) { mutableStateOf(base?.repeatDays ?: emptySet()) }
    var soundUri by remember(base) { mutableStateOf(base?.soundUri) }
    var vibrate by remember(base) { mutableStateOf(base?.vibrate ?: true) }
    var enabledTasks by remember(base) { mutableStateOf(base?.enabledMediumTasks ?: MediumTaskType.values().toSet()) }
    var exerciseType by remember(base) { mutableStateOf(base?.exerciseType ?: ExerciseType.SQUATS) }
    var exerciseSeconds by remember(base) { mutableFloatStateOf((base?.exerciseDurationSeconds ?: 300).toFloat()) }

    // Strict category state variables
    var strictCategory by remember(base) { mutableStateOf(base?.strictCategory ?: StrictExerciseCategory.PHYSICAL) }
    var mentalExerciseType by remember(base) { mutableStateOf(base?.mentalExerciseType ?: MentalExerciseType.MEMORY_GRID) }

    val soundPicker = rememberSoundPickerLauncher { uri -> soundUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (base == null) "New alarm" else "Edit alarm") },
                actions = {
                    TextButton(onClick = {
                        viewModel.save(
                            Alarm(
                                id = base?.id ?: 0L,
                                hour = hour, minute = minute, label = label,
                                isEnabled = base?.isEnabled ?: true,
                                repeatDays = days, mode = mode, soundUri = soundUri,
                                vibrate = vibrate, enabledMediumTasks = enabledTasks,
                                exerciseDurationSeconds = exerciseSeconds.toInt(),
                                exerciseType = exerciseType,
                                strictCategory = strictCategory,
                                mentalExerciseType = mentalExerciseType
                            )
                        )
                        onDone()
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedButton(onClick = {
                TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, false).show()
            }) {
                Text("Time: ${formatTime12h(hour, minute)}")
            }

            OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label") })

            Text("Repeat", style = MaterialTheme.typography.titleSmall)
            DaySelector(selected = days, onChange = { days = it })

            Text("Mode", style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow {
                AlarmMode.values().forEachIndexed { index, m ->
                    SegmentedButton(
                        selected = mode == m,
                        onClick = { mode = m },
                        shape = SegmentedButtonDefaults.itemShape(index, AlarmMode.values().size)
                    ) { Text(m.name.lowercase().replaceFirstChar { it.uppercase() }) }
                }
            }

            when (mode) {
                AlarmMode.EASY -> Text(
                    "Rings normally; dismiss or snooze with one tap.",
                    style = MaterialTheme.typography.bodySmall
                )
                AlarmMode.MEDIUM -> MediumTaskPicker(enabledTasks) { enabledTasks = it }
                AlarmMode.STRICT -> StrictConfig(
                    strictCategory = strictCategory,
                    onStrictCategoryChange = { strictCategory = it },
                    exerciseType = exerciseType,
                    onExerciseTypeChange = { exerciseType = it },
                    mentalExerciseType = mentalExerciseType,
                    onMentalExerciseTypeChange = { mentalExerciseType = it },
                    seconds = exerciseSeconds,
                    onSecondsChange = { exerciseSeconds = it }
                )
            }

            Text("Sound", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = { soundPicker() }) {
                Text(if (soundUri == null) "Default alarm sound" else "Custom sound selected")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = vibrate, onCheckedChange = { vibrate = it })
                Text("Vibrate")
            }
        }
    }
}

@Composable
private fun DaySelector(selected: Set<Int>, onChange: (Set<Int>) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..7).forEach { iso ->
            val label = DayOfWeek.of(iso).getDisplayName(TextStyle.NARROW, Locale.getDefault())
            FilterChip(
                selected = selected.contains(iso),
                onClick = {
                    onChange(if (selected.contains(iso)) selected - iso else selected + iso)
                },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun MediumTaskPicker(enabled: Set<MediumTaskType>, onChange: (Set<MediumTaskType>) -> Unit) {
    Column {
        Text(
            "The app will randomly pick ONE of your checked tasks each time this alarm rings.",
            style = MaterialTheme.typography.bodySmall
        )
        MediumTaskType.values().forEach { task ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = enabled.contains(task),
                    onCheckedChange = {
                        val next = if (it) enabled + task else enabled - task
                        if (next.isNotEmpty()) onChange(next)
                    }
                )
                Text(taskLabel(task))
            }
        }
    }
}

private fun taskLabel(task: MediumTaskType) = when (task) {
    MediumTaskType.MATH -> "Math problem"
    MediumTaskType.READING -> "Read a passage"
    MediumTaskType.LYRICS -> "Sing along to lyrics"
    MediumTaskType.BARCODE -> "Scan a barcode"
}

@Composable
private fun StrictConfig(
    strictCategory: StrictExerciseCategory,
    onStrictCategoryChange: (StrictExerciseCategory) -> Unit,
    exerciseType: ExerciseType,
    onExerciseTypeChange: (ExerciseType) -> Unit,
    mentalExerciseType: MentalExerciseType,
    onMentalExerciseTypeChange: (MentalExerciseType) -> Unit,
    seconds: Float,
    onSecondsChange: (Float) -> Unit
) {
    Column {
        Text(
            "Strict mode requires completing a verified physical workout or mental puzzle before the alarm can be dismissed.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))

        Text("Strict Category", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        SingleChoiceSegmentedButtonRow {
            StrictExerciseCategory.values().forEachIndexed { index, cat ->
                SegmentedButton(
                    selected = strictCategory == cat,
                    onClick = { onStrictCategoryChange(cat) },
                    shape = SegmentedButtonDefaults.itemShape(index, StrictExerciseCategory.values().size)
                ) {
                    Text(if (cat == StrictExerciseCategory.PHYSICAL) "Physical" else "Mental")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (strictCategory == StrictExerciseCategory.PHYSICAL) {
            Text("Physical Exercise Type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow {
                ExerciseType.values().forEachIndexed { index, e ->
                    SegmentedButton(
                        selected = exerciseType == e,
                        onClick = { onExerciseTypeChange(e) },
                        shape = SegmentedButtonDefaults.itemShape(index, ExerciseType.values().size)
                    ) { Text(if (e == ExerciseType.SQUATS) "Squats" else "Jumping jacks") }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Duration: ${seconds.toInt() / 60}m ${seconds.toInt() % 60}s")
            Slider(value = seconds, onValueChange = onSecondsChange, valueRange = 60f..900f, steps = 13)
        } else {
            Text("Mental Challenge Type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow {
                MentalExerciseType.values().forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = mentalExerciseType == type,
                        onClick = { onMentalExerciseTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index, MentalExerciseType.values().size)
                    ) {
                        Text(if (type == MentalExerciseType.MEMORY_GRID) "Memory Grid" else "Maze")
                    }
                }
            }
        }
    }
}
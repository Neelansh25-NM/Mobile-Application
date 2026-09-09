package com.example.alarmboss.ui.ringing

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmboss.alarm.AlarmReceiver
import com.example.alarmboss.alarm.AlarmScheduler
import com.example.alarmboss.alarm.AlarmService
import com.example.alarmboss.data.Alarm
import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.MediumTaskType
import com.example.alarmboss.data.MentalExerciseType
import com.example.alarmboss.data.StrictExerciseCategory
import com.example.alarmboss.ui.ringing.tasks.BarcodeTaskScreen
import com.example.alarmboss.ui.ringing.tasks.LyricsTaskScreen
import com.example.alarmboss.ui.ringing.tasks.MathTaskScreen
import com.example.alarmboss.ui.ringing.tasks.MazeTaskScreen
import com.example.alarmboss.ui.ringing.tasks.MemoryTaskScreen
import com.example.alarmboss.ui.ringing.tasks.ReadingTaskScreen
import com.example.alarmboss.ui.theme.AlarmBossTheme

/**
 * Full-screen activity shown over the lock screen when an alarm fires. This is the ONLY
 * place the alarm can be dismissed from -- back/home are suppressed where the platform
 * allows it, and the underlying AlarmService keeps sound/vibration going until dismiss()
 * is actually reached, so the mode's task genuinely has to be completed.
 */
class AlarmRingingActivity : ComponentActivity() {

    private val viewModel: AlarmRingingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        // Block the back gesture/button so the alarm can't be dismissed without completing its task.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* no-op by design */ }
        })

        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)
        viewModel.load(alarmId)

        setContent {
            AlarmBossTheme {
                val alarm by viewModel.alarm.collectAsState()
                Surface(Modifier.fillMaxSize()) {
                    alarm?.let {
                        RingingContent(it, onDismiss = ::dismissAlarm, onSnooze = ::snoozeAlarm)
                    } ?: LoadingContent()
                }
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun dismissAlarm() {
        AlarmService.stop(this)
        finish()
    }

    /** Only offered in Easy mode: stops the current ring and re-fires the alarm in 5 minutes. */
    private fun snoozeAlarm() {
        viewModel.alarm.value?.let { alarm ->
            AlarmScheduler(this).scheduleOneOffInMinutes(alarm, 5)
        }
        AlarmService.stop(this)
        finish()
    }
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun RingingContent(alarm: Alarm, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    when (alarm.mode) {
        AlarmMode.EASY -> EasyDismissScreen(alarm, onDismiss, onSnooze)
        AlarmMode.MEDIUM -> {
            val task = remember(alarm.id) { alarm.enabledMediumTasks.randomOrNull() ?: MediumTaskType.MATH }
            when (task) {
                MediumTaskType.MATH -> MathTaskScreen(onSolved = onDismiss)
                MediumTaskType.READING -> ReadingTaskScreen(onDone = onDismiss)
                MediumTaskType.LYRICS -> LyricsTaskScreen(onDone = onDismiss)
                MediumTaskType.BARCODE -> BarcodeTaskScreen(onScanned = onDismiss)
            }
        }
        AlarmMode.STRICT -> {
            if (alarm.strictCategory == StrictExerciseCategory.MENTAL) {
                when (alarm.mentalExerciseType) {
                    MentalExerciseType.MEMORY_GRID -> MemoryTaskScreen(onSolved = onDismiss)
                    MentalExerciseType.MAZE -> MazeTaskScreen(onSolved = onDismiss)
                }
            } else {
                StrictExerciseScreen(
                    initialExercise = alarm.exerciseType,
                    durationSeconds = alarm.exerciseDurationSeconds,
                    onComplete = onDismiss
                )
            }
        }
    }
}

@Composable
private fun EasyDismissScreen(alarm: Alarm, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(alarm.label.ifBlank { "Alarm" }, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDismiss) { Text("Dismiss") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onSnooze) { Text("Snooze 5 min") }
    }
}
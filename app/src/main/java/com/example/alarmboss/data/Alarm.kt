package com.example.alarmboss.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
enum class AlarmMode { EASY, MEDIUM, STRICT }

enum class MediumTaskType { MATH, READING, LYRICS, BARCODE, PATTERN_LOCK, WORD_SCRAMBLE, STEPS }

// Add these enums for Strict Mode categorization
enum class StrictExerciseCategory { PHYSICAL, MENTAL }

enum class MentalExerciseType { MEMORY_GRID, MAZE}

enum class ExerciseType { SQUATS, JUMPING_JACKS }

@Entity(tableName = "alarms")
data class Alarm @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet(),
    val mode: AlarmMode = AlarmMode.EASY,
    val soundUri: String? = null,
    val vibrate: Boolean = true,
    val enabledMediumTasks: Set<MediumTaskType> = MediumTaskType.values().toSet(),
    val exerciseDurationSeconds: Int = 300,
    val exerciseType: ExerciseType = ExerciseType.SQUATS,

    // Add these fields referenced in AlarmRingingActivity
    val strictCategory: StrictExerciseCategory = StrictExerciseCategory.PHYSICAL,
    val mentalExerciseType: MentalExerciseType = MentalExerciseType.MEMORY_GRID,
    val dynamicBrightness: Boolean = false,
    val flashLightBlink: Boolean = false,
    val gradualVolume: Boolean = false,
    val sunriseMode: Boolean = false,
    val wakeCheck: Boolean = false,
    val morningBriefing: Boolean = false
) {
    /** In Medium and Strict modes, the safeguard awake check is locked and always active. */
    val isWakeCheckEffective: Boolean
        get() = mode == AlarmMode.MEDIUM || mode == AlarmMode.STRICT || wakeCheck
}

fun isAlarmLocked(
    alarmHour: Int,
    alarmMinute: Int,
    isEnabled: Boolean,
    now: Calendar = Calendar.getInstance()
): Boolean {
    if (!isEnabled) return false

    val alarmTime = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, alarmHour)
        set(Calendar.MINUTE, alarmMinute)
        set(Calendar.SECOND, 0)
    }

    // If the alarm time has already passed today, it means it's set for tomorrow
    if (alarmTime.before(now)) {
        alarmTime.add(Calendar.DAY_OF_YEAR, 1)
    }

    val timeDifferenceMillis = alarmTime.timeInMillis - now.timeInMillis
    val twoHoursInMillis = 2 * 60 * 60 * 1000

    return timeDifferenceMillis in 0..twoHoursInMillis
}
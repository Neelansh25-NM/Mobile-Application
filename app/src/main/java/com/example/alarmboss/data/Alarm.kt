package com.example.alarmboss.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlarmMode { EASY, MEDIUM, STRICT }

enum class MediumTaskType { MATH, READING, LYRICS, BARCODE }

enum class ExerciseType { SQUATS, JUMPING_JACKS }

/**
 * Days stored as ISO day-of-week ints: 1=Monday .. 7=Sunday (matches java.time.DayOfWeek).
 * Empty set = one-time alarm (fires once, then disables itself).
 */
@Entity(tableName = "alarms")
data class Alarm(
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
    val exerciseType: ExerciseType = ExerciseType.SQUATS
)

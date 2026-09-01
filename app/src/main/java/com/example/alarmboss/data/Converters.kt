package com.example.alarmboss.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntSet(value: Set<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntSet(value: String): Set<Int> =
        if (value.isBlank()) emptySet() else value.split(",").map { it.trim().toInt() }.toSet()

    @TypeConverter
    fun fromMode(value: AlarmMode): String = value.name

    @TypeConverter
    fun toMode(value: String): AlarmMode = AlarmMode.valueOf(value)

    @TypeConverter
    fun fromExerciseType(value: ExerciseType): String = value.name

    @TypeConverter
    fun toExerciseType(value: String): ExerciseType = ExerciseType.valueOf(value)

    @TypeConverter
    fun fromTaskSet(value: Set<MediumTaskType>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toTaskSet(value: String): Set<MediumTaskType> =
        if (value.isBlank()) emptySet()
        else value.split(",").map { MediumTaskType.valueOf(it.trim()) }.toSet()
}

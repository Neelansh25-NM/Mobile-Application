package com.example.alarmboss.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wake_events")
data class WakeEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    val triggeredAt: Long,
    val dismissedAt: Long,
    val challengeType: String,
    val difficulty: Int,
    val xpEarned: Int,
    val reactionTimeMs: Long,
    val completionTimeMs: Long,
    val isBossFight: Boolean
)

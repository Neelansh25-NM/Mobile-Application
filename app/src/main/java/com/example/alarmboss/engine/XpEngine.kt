package com.example.alarmboss.engine

import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.StrictExerciseCategory
import com.example.alarmboss.data.UserProfile
import kotlin.math.floor
import kotlin.math.sqrt

object XpEngine {
    fun calculateXp(
        mode: AlarmMode,
        isBossFight: Boolean,
        streak: Int,
        completionTimeMs: Long,
        isPerfectWeek: Boolean = false,
        strictCategory: StrictExerciseCategory? = null
    ): Int {
        if (isBossFight) return 100

        var xp = when (mode) {
            AlarmMode.EASY -> 10
            AlarmMode.MEDIUM -> 25
            AlarmMode.STRICT -> if (strictCategory == StrictExerciseCategory.PHYSICAL) 50 else 40
        }

        // Streak bonus
        xp += 5 * streak

        // Speed bonus
        if (completionTimeMs < 30_000) {
            xp += 15
        }

        // Perfect week bonus
        if (isPerfectWeek) {
            xp += 200
        }

        return xp
    }

    fun calculateLevel(totalXp: Int): Int {
        return floor(sqrt(totalXp.toDouble() / 50.0)).toInt() + 1
    }
}

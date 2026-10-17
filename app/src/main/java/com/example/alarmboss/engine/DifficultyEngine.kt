package com.example.alarmboss.engine

import com.example.alarmboss.data.UserProfile

object DifficultyEngine {
    // Scales 1-10 based on level + streak
    fun calculate(level: Int, streak: Int): Int {
        val baseDifficulty = (level / 3) + (streak / 5)
        return baseDifficulty.coerceIn(1, 10)
    }

    fun calculate(profile: UserProfile): Int {
        return calculate(profile.level, profile.currentStreak)
    }
}

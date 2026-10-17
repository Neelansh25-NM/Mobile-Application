package com.example.alarmboss.engine

import com.example.alarmboss.data.UserProfile

object BossFightManager {
    /** Boss fight triggers every 7th day of a streak. [streak] is the streak including today. */
    fun isBossFightDay(streak: Int): Boolean = streak > 0 && streak % 7 == 0

    fun isBossFightDay(profile: UserProfile): Boolean = isBossFightDay(profile.currentStreak)
}

package com.example.alarmboss.engine

import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.StrictExerciseCategory

/** What a particular ring asks of the user, decided once from the profile snapshot. */
data class RingPlan(val isBossFight: Boolean, val difficulty: Int)

object RingPlanner {

    /**
     * Easy alarms stay one tap by contract; boss fights replace the task on Medium and Strict
     * ones. [projectedStreak] is the streak including today's wake-up.
     */
    fun plan(mode: AlarmMode, level: Int, projectedStreak: Int): RingPlan = RingPlan(
        isBossFight = mode != AlarmMode.EASY && BossFightManager.isBossFightDay(projectedStreak),
        difficulty = DifficultyEngine.calculate(level, projectedStreak)
    )

    /** The label stored with a wake event, so stats can later tell challenge kinds apart. */
    fun challengeType(mode: AlarmMode, strictCategory: StrictExerciseCategory, plan: RingPlan): String = when {
        plan.isBossFight -> "BOSS_FIGHT"
        mode == AlarmMode.EASY -> "EASY"
        mode == AlarmMode.MEDIUM -> "MEDIUM"
        strictCategory == StrictExerciseCategory.PHYSICAL -> "STRICT_PHYSICAL"
        else -> "STRICT_MENTAL"
    }
}

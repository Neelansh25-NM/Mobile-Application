package com.example.alarmboss.engine

import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.StrictExerciseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RingPlannerTest {

    @Test
    fun `easy alarms never get a boss fight even on a seventh day`() {
        assertFalse(RingPlanner.plan(AlarmMode.EASY, level = 1, projectedStreak = 7).isBossFight)
    }

    @Test
    fun `medium and strict alarms get a boss fight on every seventh day`() {
        assertTrue(RingPlanner.plan(AlarmMode.MEDIUM, 1, 7).isBossFight)
        assertTrue(RingPlanner.plan(AlarmMode.STRICT, 1, 14).isBossFight)
    }

    @Test
    fun `no boss fight on other days`() {
        assertFalse(RingPlanner.plan(AlarmMode.MEDIUM, 1, 6).isBossFight)
        assertFalse(RingPlanner.plan(AlarmMode.STRICT, 1, 1).isBossFight)
    }

    @Test
    fun `difficulty follows level and projected streak`() {
        assertEquals(4, RingPlanner.plan(AlarmMode.MEDIUM, level = 6, projectedStreak = 10).difficulty)
    }

    @Test
    fun `challenge type reflects the mode and the boss fight`() {
        val normal = RingPlan(isBossFight = false, difficulty = 1)
        val boss = RingPlan(isBossFight = true, difficulty = 1)
        assertEquals("EASY", RingPlanner.challengeType(AlarmMode.EASY, StrictExerciseCategory.PHYSICAL, normal))
        assertEquals("MEDIUM", RingPlanner.challengeType(AlarmMode.MEDIUM, StrictExerciseCategory.PHYSICAL, normal))
        assertEquals("STRICT_PHYSICAL", RingPlanner.challengeType(AlarmMode.STRICT, StrictExerciseCategory.PHYSICAL, normal))
        assertEquals("STRICT_MENTAL", RingPlanner.challengeType(AlarmMode.STRICT, StrictExerciseCategory.MENTAL, normal))
        assertEquals("BOSS_FIGHT", RingPlanner.challengeType(AlarmMode.STRICT, StrictExerciseCategory.MENTAL, boss))
    }
}

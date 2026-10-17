package com.example.alarmboss.ui.ringing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmboss.AlarmBossApp
import com.example.alarmboss.alarm.AlarmScheduler
import com.example.alarmboss.data.Alarm
import com.example.alarmboss.data.AlarmMode
import com.example.alarmboss.data.UserProfile
import com.example.alarmboss.data.WakeEvent
import com.example.alarmboss.engine.ProgressionEngine
import com.example.alarmboss.engine.RingPlan
import com.example.alarmboss.engine.RingPlanner
import com.example.alarmboss.util.SnoozeSettings
import com.example.alarmboss.util.StreakManager
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Everything the ringing screen needs, resolved once when the ring starts. */
data class RingSession(
    val alarm: Alarm,
    val plan: RingPlan,
    val currentStreak: Int,
    val snoozeMinutes: Int
)

class AlarmRingingViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AlarmBossApp
    private val repository = app.repository
    private val profileRepository = app.profileRepository
    private val streakManager = StreakManager(application)
    private val snoozeSettings = SnoozeSettings(application)

    private val _session = MutableStateFlow<RingSession?>(null)
    val session: StateFlow<RingSession?> = _session

    // Snapshot taken before this wake-up is recorded, so boss day and difficulty stay stable
    // for the whole ring instead of shifting once the profile updates on dismiss.
    private var profileSnapshot: UserProfile? = null

    private val ringStartedAt: Long = System.currentTimeMillis()
    private var firstTouchAt: Long = 0L
    private var completed = false

    fun load(id: Long) {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            // Streak including today, fixed now so a dismiss after midnight can't change the plan.
            val projectedStreak = streakManager.projectedStreak()
            val alarm = repository.getAlarm(id) ?: return@launch
            profileSnapshot = profile
            _session.value = RingSession(
                alarm = alarm,
                plan = RingPlanner.plan(alarm.mode, profile.level, projectedStreak),
                currentStreak = streakManager.getCurrentStreak(),
                snoozeMinutes = snoozeSettings.minutes
            )
        }
    }

    fun onFirstTouch() {
        if (firstTouchAt == 0L) firstTouchAt = System.currentTimeMillis()
    }

    var lastWakeResult: StreakManager.StreakResult? = null
        private set

    /**
     * Records a genuine wake-up: the alarm's task (if any) was actually completed, not snoozed
     * away. Returns false if it was already recorded, so a double tap on the final step can't
     * award XP twice.
     */
    fun completeWakeUp(): Boolean {
        if (completed) return false
        completed = true

        val nowTime = java.time.LocalTime.now()
        val streakResult = streakManager.recordSuccessfulWakeUpDetailed(today = LocalDate.now(), time = nowTime)
        lastWakeResult = streakResult
        val newStreak = streakResult.currentStreak

        val session = _session.value ?: return true
        val profile = profileSnapshot ?: return true

        val alarm = session.alarm
        val plan = session.plan
        val now = System.currentTimeMillis()
        val completionMs = now - ringStartedAt
        val reactionMs = (firstTouchAt.takeIf { it > 0L } ?: now) - ringStartedAt

        val outcome = ProgressionEngine.applyWakeUp(
            profile = profile,
            mode = alarm.mode,
            strictCategory = alarm.strictCategory.takeIf { alarm.mode == AlarmMode.STRICT },
            isBossFight = plan.isBossFight,
            newStreak = newStreak,
            reactionMs = reactionMs,
            completionMs = completionMs,
            today = LocalDate.now()
        )
        val event = WakeEvent(
            alarmId = alarm.id,
            triggeredAt = ringStartedAt,
            dismissedAt = now,
            challengeType = RingPlanner.challengeType(alarm.mode, alarm.strictCategory, plan),
            difficulty = plan.difficulty,
            xpEarned = outcome.xpEarned,
            reactionTimeMs = reactionMs,
            completionTimeMs = completionMs,
            isBossFight = plan.isBossFight
        )

        // App scope, not viewModelScope: the screen finishes right after this and would cancel it.
        app.appScope.launch {
            profileRepository.recordWakeUp(event, outcome.profile)
            if (alarm.repeatDays.isNotEmpty()) {
                AlarmScheduler(app).schedule(alarm)
            } else {
                repository.setEnabled(alarm.id, false)
            }
            // Enforce wake check safeguard: always ON for Medium & Strict modes, or if manually enabled
            if (alarm.isWakeCheckEffective) {
                AlarmScheduler(app).scheduleWakeCheck(alarm.id, 5)
            }
        }
        return true
    }

    /** Only offered in Easy mode: re-fires the alarm after the chosen snooze length, with a snooze XP penalty. */
    fun snooze() {
        val session = _session.value ?: return
        AlarmScheduler(app).scheduleOneOffInMinutes(session.alarm, session.snoozeMinutes)
        app.appScope.launch {
            val currentProfile = profileRepository.getProfile()
            val penalizedProfile = currentProfile.copy(
                totalXp = kotlin.math.max(0, currentProfile.totalXp - 15)
            )
            profileRepository.updateProfile(penalizedProfile)
        }
    }

    fun addQuestBonusXp(bonusXp: Int) {
        if (bonusXp <= 0) return
        app.appScope.launch {
            val currentProfile = profileRepository.getProfile()
            val newTotalXp = currentProfile.totalXp + bonusXp
            val updated = currentProfile.copy(
                totalXp = newTotalXp,
                level = com.example.alarmboss.engine.XpEngine.calculateLevel(newTotalXp)
            )
            profileRepository.updateProfile(updated)
        }
    }
}

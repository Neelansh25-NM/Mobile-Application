package com.example.alarmboss.ui.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmboss.AlarmBossApp
import com.example.alarmboss.alarm.AlarmScheduler
import com.example.alarmboss.data.Alarm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.alarmboss.util.StreakManager

/** The result of looking up the alarm an edit screen was opened for. */
sealed interface EditTarget {
    data object Loading : EditTarget
    data object Missing : EditTarget
    data class Existing(val alarm: Alarm) : EditTarget
}

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val streakManager = StreakManager(application)

    private val repository = (application as AlarmBossApp).repository
    private val scheduler = AlarmScheduler(application)

    private val _streak = MutableStateFlow(streakManager.getCurrentStreak())
    val streak: StateFlow<Int> = _streak.asStateFlow()
    val alarms: StateFlow<List<Alarm>> = repository.observeAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editTarget = MutableStateFlow<EditTarget>(EditTarget.Loading)
    val editTarget: StateFlow<EditTarget> = _editTarget.asStateFlow()
    private var requestedEditId: Long? = null

    fun loadStreak() {
        _streak.value = streakManager.getCurrentStreak()
    }

    /**
     * Resolves the alarm to edit. Idempotent for the same id: the screen asks again after every
     * rotation, and answering "Loading" a second time would tear down a form the user is typing in.
     */
    fun loadForEdit(id: Long) {
        if (requestedEditId == id) return
        requestedEditId = id
        viewModelScope.launch {
            _editTarget.value = repository.getAlarm(id)?.let { EditTarget.Existing(it) } ?: EditTarget.Missing
        }
    }

    fun save(alarm: Alarm) = viewModelScope.launch {
        val id = repository.save(alarm)
        val saved = alarm.copy(id = if (alarm.id == 0L) id else alarm.id)
        if (saved.isEnabled) scheduler.schedule(saved) else scheduler.cancel(saved.id)
    }

    fun delete(alarm: Alarm) = viewModelScope.launch {
        scheduler.cancel(alarm.id)
        repository.delete(alarm)
    }

    fun setEnabled(alarm: Alarm, enabled: Boolean) = viewModelScope.launch {
        repository.setEnabled(alarm.id, enabled)
        if (enabled) scheduler.schedule(alarm.copy(isEnabled = true)) else scheduler.cancel(alarm.id)
    }
}

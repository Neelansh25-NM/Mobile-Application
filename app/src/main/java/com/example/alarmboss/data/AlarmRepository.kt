package com.example.alarmboss.data

import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val dao: AlarmDao) {
    fun observeAlarms(): Flow<List<Alarm>> = dao.observeAlarms()
    suspend fun getAlarm(id: Long): Alarm? = dao.getAlarm(id)
    suspend fun getEnabledAlarms(): List<Alarm> = dao.getEnabledAlarms()
    suspend fun save(alarm: Alarm): Long = dao.upsert(alarm)
    suspend fun delete(alarm: Alarm) = dao.delete(alarm)
    suspend fun setEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled)
}

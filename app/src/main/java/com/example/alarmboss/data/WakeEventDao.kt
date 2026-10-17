package com.example.alarmboss.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WakeEventDao {
    @Insert
    suspend fun insert(event: WakeEvent): Long

    @Query("SELECT * FROM wake_events ORDER BY triggeredAt DESC")
    fun observeAll(): Flow<List<WakeEvent>>

    @Query("SELECT * FROM wake_events ORDER BY triggeredAt DESC LIMIT 1")
    suspend fun getLastEvent(): WakeEvent?
}

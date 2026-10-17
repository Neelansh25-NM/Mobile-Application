package com.example.alarmboss.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(
    private val profileDao: UserProfileDao,
    private val wakeEventDao: WakeEventDao
) {
    val profile: Flow<UserProfile> = profileDao.observeProfile().map { it ?: UserProfile() }

    suspend fun getProfile(): UserProfile {
        return profileDao.getProfile() ?: UserProfile()
    }

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.insertOrUpdate(profile)
    }

    suspend fun recordWakeEvent(event: WakeEvent) {
        wakeEventDao.insert(event)
    }

    /** Stores a completed wake-up and the profile it produced together. */
    suspend fun recordWakeUp(event: WakeEvent, updatedProfile: UserProfile) {
        wakeEventDao.insert(event)
        profileDao.insertOrUpdate(updatedProfile)
    }
    
    fun observeWakeEvents(): Flow<List<WakeEvent>> = wakeEventDao.observeAll()
}

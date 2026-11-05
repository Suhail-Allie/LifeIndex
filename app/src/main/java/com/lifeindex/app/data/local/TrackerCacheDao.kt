package com.lifeindex.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerCacheDao {
    @Query("SELECT * FROM tracker_cache WHERE userId = :userId AND ((isDeleted = 0 AND isArchived = 0) OR syncStatus = 'CONFLICT') ORDER BY updatedAt DESC")
    fun observeTrackers(userId: String): Flow<List<TrackerCacheEntity>>

    @Query("SELECT * FROM tracker_cache WHERE userId = :userId AND trackerId = :trackerId LIMIT 1")
    fun observeTracker(userId: String, trackerId: String): Flow<TrackerCacheEntity?>

    @Query("SELECT * FROM tracker_cache WHERE userId = :userId AND trackerId = :trackerId LIMIT 1")
    suspend fun getTracker(userId: String, trackerId: String): TrackerCacheEntity?

    @Query("SELECT * FROM tracker_cache WHERE userId = :userId AND syncStatus = 'PENDING' ORDER BY updatedAt ASC")
    suspend fun getPending(userId: String): List<TrackerCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TrackerCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TrackerCacheEntity>)

    @Query("DELETE FROM tracker_cache WHERE userId = :userId AND trackerId = :trackerId")
    suspend fun deleteTracker(userId: String, trackerId: String)
}
package com.lifeindex.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracker_cache",
    indices = [Index(value = ["userId"]), Index(value = ["syncStatus"])]
)
data class TrackerCacheEntity(
    @PrimaryKey val trackerId: String,
    val userId: String,
    val payload: String,
    val syncStatus: String,
    val pendingAction: String?,
    val baseVersion: Int,
    val isDeleted: Boolean,
    val isArchived: Boolean,
    val serverSnapshot: String?,
    val updatedAt: Long
)
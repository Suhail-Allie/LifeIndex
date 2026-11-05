package com.lifeindex.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.lifeindex.app.data.local.LifeIndexDatabase
import com.lifeindex.app.data.local.TrackerCacheDao
import com.lifeindex.app.data.local.TrackerCacheEntity
import com.lifeindex.app.data.model.CreateTrackerRequest
import com.lifeindex.app.data.model.MessageResponse
import com.lifeindex.app.data.model.TrackerData
import com.lifeindex.app.data.model.TrackerResponse
import com.lifeindex.app.data.model.TrackerSyncOperation
import com.lifeindex.app.data.model.TrackerSyncPayload
import com.lifeindex.app.data.model.TrackerSyncRequest
import com.lifeindex.app.data.model.TrackersResponse
import com.lifeindex.app.data.remote.RetrofitClient
import com.lifeindex.app.data.sync.TrackerSyncScheduler
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class TrackerRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao: TrackerCacheDao = LifeIndexDatabase.getInstance(appContext).trackerCacheDao()
    private val api = RetrofitClient.api
    private val gson = Gson()

    fun observeTrackers(userId: String): Flow<List<TrackerData>> =
        dao.observeTrackers(userId).map { rows -> rows.map(::toTrackerData) }

    fun observeTracker(userId: String, trackerId: String): Flow<TrackerData?> =
        dao.observeTracker(userId, trackerId).map { row -> row?.let(::toTrackerData) }

    suspend fun createTracker(userId: String, request: CreateTrackerRequest): TrackerData {
        val now = Instant.now().toString()
        val tracker = TrackerData(
            id = UUID.randomUUID().toString(),
            user_id = userId,
            template_id = request.templateId,
            category_id = request.categoryId,
            title = request.title,
            tracker_type = request.trackerType,
            status = request.status ?: "ACTIVE",
            priority = request.priority ?: "NORMAL",
            important_date = request.importantDate,
            notes = request.notes,
            is_archived = false,
            created_at = now,
            updated_at = now,
            version = 1,
            sync_state = "PENDING"
        )
        dao.upsert(tracker.toEntity(userId, "PENDING", "CREATE", 0, false, null))
        TrackerSyncScheduler.enqueue(appContext)
        return tracker
    }

    suspend fun refreshTrackers(userId: String, accessToken: String): Boolean {
        val response = api.getTrackers("Bearer $accessToken")
        if (!response.isSuccessful) return false
        val remote = response.body()?.trackers ?: emptyList()
        remote.forEach { tracker ->
            val cached = dao.getTracker(userId, tracker.id)
            if (cached == null || cached.syncStatus == "SYNCED") {
                dao.upsert(tracker.toEntity(userId, "SYNCED", null, tracker.version, false, null))
            }
        }
        return true
    }

    suspend fun refreshTracker(userId: String, accessToken: String, trackerId: String): Boolean {
        val response = api.getTrackerById("Bearer $accessToken", trackerId)
        if (!response.isSuccessful) return false
        val tracker = response.body()?.tracker ?: return false
        val cached = dao.getTracker(userId, trackerId)
        if (cached == null || cached.syncStatus == "SYNCED") {
            dao.upsert(tracker.toEntity(userId, "SYNCED", null, tracker.version, false, null))
        }
        return true
    }

    suspend fun updateTracker(userId: String, trackerId: String, request: CreateTrackerRequest): TrackerData? {
        val cached = dao.getTracker(userId, trackerId) ?: return null
        val current = toTrackerData(cached)
        val updated = current.copy(
            title = request.title,
            tracker_type = request.trackerType,
            template_id = request.templateId ?: current.template_id,
            category_id = request.categoryId ?: current.category_id,
            status = request.status ?: current.status,
            priority = request.priority ?: current.priority,
            important_date = request.importantDate ?: current.important_date,
            notes = request.notes,
            updated_at = Instant.now().toString(),
            sync_state = "PENDING"
        )
        val action = if (cached.pendingAction == "CREATE") "CREATE" else "UPDATE"
        dao.upsert(cached.copy(
            payload = gson.toJson(updated),
            syncStatus = "PENDING",
            pendingAction = action,
            serverSnapshot = null,
            updatedAt = System.currentTimeMillis()
        ))
        TrackerSyncScheduler.enqueue(appContext)
        return updated
    }

    suspend fun archiveTracker(userId: String, trackerId: String): Boolean {
        val cached = dao.getTracker(userId, trackerId) ?: return false
        val updated = toTrackerData(cached).copy(
            is_archived = true,
            updated_at = Instant.now().toString(),
            sync_state = "PENDING"
        )
        val action = if (cached.pendingAction == "CREATE") "CREATE" else "UPDATE"
        dao.upsert(cached.copy(
            payload = gson.toJson(updated),
            syncStatus = "PENDING",
            pendingAction = action,
            isArchived = true,
            serverSnapshot = null,
            updatedAt = System.currentTimeMillis()
        ))
        TrackerSyncScheduler.enqueue(appContext)
        return true
    }

    suspend fun deleteTracker(userId: String, trackerId: String): Boolean {
        val cached = dao.getTracker(userId, trackerId) ?: return false
        if (cached.pendingAction == "CREATE" && cached.baseVersion == 0) {
            dao.deleteTracker(userId, trackerId)
        } else {
            dao.upsert(cached.copy(
                syncStatus = "PENDING",
                pendingAction = "DELETE",
                isDeleted = true,
                serverSnapshot = null,
                updatedAt = System.currentTimeMillis()
            ))
            TrackerSyncScheduler.enqueue(appContext)
        }
        return true
    }

    suspend fun syncPending(userId: String, accessToken: String): Boolean {
        val pending = dao.getPending(userId)
        if (pending.isEmpty()) return true

        val operations = pending.map { row ->
            val tracker = toTrackerData(row)
            val action = row.pendingAction ?: "UPDATE"
            val payload = if (action == "DELETE") null else TrackerSyncPayload(
                title = tracker.title,
                trackerType = tracker.tracker_type,
                templateId = tracker.template_id,
                categoryId = tracker.category_id,
                status = tracker.status,
                priority = tracker.priority,
                importantDate = tracker.important_date,
                notes = tracker.notes,
                isArchived = tracker.is_archived
            )
            TrackerSyncOperation(
                id = row.trackerId,
                action = action,
                baseVersion = row.baseVersion,
                payload = payload
            )
        }

        val response = api.syncTrackers("Bearer $accessToken", TrackerSyncRequest(operations))
        if (!response.isSuccessful) return false

        val syncResults = response.body()?.results.orEmpty()
        if (syncResults.size != pending.size) return false
        var allHandled = true
        syncResults.forEach { result ->
            val local = dao.getTracker(userId, result.id) ?: return@forEach
            when (result.status.uppercase()) {
                "SYNCED" -> {
                    if (local.pendingAction == "DELETE") {
                        dao.deleteTracker(userId, result.id)
                    } else {
                        val server = result.tracker
                        if (server != null) {
                            dao.upsert(server.toEntity(userId, "SYNCED", null, server.version, false, null))
                        } else {
                            allHandled = false
                        }
                    }
                }
                "CONFLICT" -> {
                    val snapshot = result.tracker
                    dao.upsert(local.copy(
                        syncStatus = "CONFLICT",
                        serverSnapshot = snapshot?.let(gson::toJson),
                        updatedAt = System.currentTimeMillis()
                    ))
                }
                else -> allHandled = false
            }
        }
        return allHandled
    }

    suspend fun useServerVersion(userId: String, trackerId: String): Boolean {
        val local = dao.getTracker(userId, trackerId) ?: return false
        val snapshot = local.serverSnapshot
        if (snapshot == null) {
            dao.deleteTracker(userId, trackerId)
            return true
        }
        val server = gson.fromJson(snapshot, TrackerData::class.java)
        dao.upsert(server.toEntity(userId, "SYNCED", null, server.version, false, null))
        return true
    }

    suspend fun keepLocalVersion(userId: String, trackerId: String): Boolean {
        val local = dao.getTracker(userId, trackerId) ?: return false
        val snapshot = local.serverSnapshot
        val serverVersion = if (snapshot == null) 0 else gson.fromJson(snapshot, TrackerData::class.java).version
        val nextAction = when {
            local.pendingAction == "DELETE" && serverVersion > 0 -> "DELETE"
            serverVersion == 0 -> "CREATE"
            else -> "UPDATE"
        }
        dao.upsert(local.copy(
            syncStatus = "PENDING",
            pendingAction = nextAction,
            baseVersion = serverVersion,
            isDeleted = nextAction == "DELETE",
            serverSnapshot = null,
            updatedAt = System.currentTimeMillis()
        ))
        TrackerSyncScheduler.enqueue(appContext)
        return true
    }

    private fun TrackerData.toEntity(
        userId: String,
        status: String,
        action: String?,
        baseVersion: Int,
        isDeleted: Boolean,
        snapshot: String?
    ) = TrackerCacheEntity(
        trackerId = id,
        userId = userId,
        payload = gson.toJson(copy(sync_state = status)),
        syncStatus = status,
        pendingAction = action,
        baseVersion = baseVersion,
        isDeleted = isDeleted,
        isArchived = is_archived,
        serverSnapshot = snapshot,
        updatedAt = System.currentTimeMillis()
    )

    private fun toTrackerData(entity: TrackerCacheEntity): TrackerData =
        gson.fromJson(entity.payload, TrackerData::class.java).copy(sync_state = entity.syncStatus)
}
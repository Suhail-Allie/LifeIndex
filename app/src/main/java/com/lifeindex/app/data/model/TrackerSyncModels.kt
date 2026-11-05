package com.lifeindex.app.data.model

data class TrackerSyncPayload(
    val title: String,
    val trackerType: String,
    val templateId: String?,
    val categoryId: String?,
    val status: String,
    val priority: String,
    val importantDate: String?,
    val notes: String?,
    val isArchived: Boolean
)

data class TrackerSyncOperation(
    val id: String,
    val action: String,
    val baseVersion: Int,
    val payload: TrackerSyncPayload? = null
)

data class TrackerSyncRequest(
    val operations: List<TrackerSyncOperation>
)

data class TrackerSyncResult(
    val id: String,
    val status: String,
    val tracker: TrackerData? = null,
    val message: String? = null
)

data class TrackerSyncResponse(
    val results: List<TrackerSyncResult>
)
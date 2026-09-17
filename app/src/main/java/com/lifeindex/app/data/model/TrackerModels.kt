package com.lifeindex.app.data.model

data class TrackerData(
    val id: String,
    val user_id: String,
    val template_id: String?,
    val category_id: String?,
    val title: String,
    val tracker_type: String,
    val status: String,
    val priority: String,
    val important_date: String?,
    val notes: String?,
    val is_archived: Boolean,
    val created_at: String,
    val updated_at: String
)

data class TrackersResponse(
    val trackers: List<TrackerData>
)

data class TrackerResponse(
    val message: String,
    val tracker: TrackerData
)

data class CreateTrackerRequest(
    val title: String,
    val trackerType: String,
    val templateId: String? = null,
    val categoryId: String? = null,
    val status: String? = "ACTIVE",
    val priority: String? = "NORMAL",
    val importantDate: String? = null,
    val notes: String? = null
)
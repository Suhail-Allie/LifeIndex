package com.lifeindex.app.data.model

data class DashboardSummary(
    val total: Int,
    val dueToday: Int,
    val upcoming: Int,
    val overdue: Int
)

data class DashboardResponse(
    val summary: DashboardSummary,
    val dueToday: List<TrackerData>,
    val upcoming: List<TrackerData>,
    val overdue: List<TrackerData>,
    val recentlyUpdated: List<TrackerData>
)
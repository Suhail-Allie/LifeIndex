package com.lifeindex.app.data.model

data class SettingsData(
    val theme: String,
    val notifications_enabled: Boolean,
    val default_reminder_minutes: Int
)

data class SettingsResponse(
    val settings: SettingsData
)

data class UpdateSettingsRequest(
    val theme: String? = null,
    val notificationsEnabled: Boolean? = null,
    val defaultReminderMinutes: Int? = null
)
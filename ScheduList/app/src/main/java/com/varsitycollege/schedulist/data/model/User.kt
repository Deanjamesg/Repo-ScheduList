package com.varsitycollege.schedulist.data.model

data class NotificationPreferences(
    val travelTimeAlerts: Boolean = true,
    val taskReminders: Boolean = true,
    val eventReminders: Boolean = true,
    val productivityAlerts: Boolean = false
)

data class User(
    val userId: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val profilePictureUrl: String? = null,
    val scheduListCalendarId: String? = null,
    val notificationPrefs: NotificationPreferences = NotificationPreferences(),
    val createdAt: Long = System.currentTimeMillis()
)

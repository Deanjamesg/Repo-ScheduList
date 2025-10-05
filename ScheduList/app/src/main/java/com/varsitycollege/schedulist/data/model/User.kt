package com.varsitycollege.schedulist.data.model

data class NotificationPreferences(
    val travelTimeAlerts: Boolean = true,
    val taskReminders: Boolean = true,
    val eventReminders: Boolean = true,
    val productivityAlerts: Boolean = false
)

data class User(
    val userId: String = "", // The unique ID provided by Firebase Authentication.
    val displayName: String? = null, // The user's Google display name.
    val email: String? = null,
    val profilePictureUrl: String? = null,
    val notificationPrefs: NotificationPreferences = NotificationPreferences()
)

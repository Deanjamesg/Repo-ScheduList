package com.varsitycollege.schedulist.data.model

data class NotificationPreferences(
    val receiveEventReminders: Boolean = true,
    val receiveTaskReminders: Boolean = true,
    val receivePromotions: Boolean = false
)

data class User(
    val userId: String = "",
    val userEmail: String = "",
    val userPassword: String = "",
    val firstName: String = "",
    val surname: String = "",
    val profilePictureUri: String? = null,
    val settings: Map<String, Any> = emptyMap(),
    val notificationPreferences: NotificationPreferences = NotificationPreferences()
)

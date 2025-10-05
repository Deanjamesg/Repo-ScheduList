package com.varsitycollege.schedulist.data.model

import java.time.OffsetDateTime

// Represents a task, compatible with Google Tasks API and app UI
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDateTime: OffsetDateTime? = null, // RFC3339 format
    val taskListId: String? = null, // Reference to TaskList
    val repeat: String? = null, // e.g., "Daily", "Weekly", etc.
    val energyLevel: String? = null // "Low", "Medium", "High"
)

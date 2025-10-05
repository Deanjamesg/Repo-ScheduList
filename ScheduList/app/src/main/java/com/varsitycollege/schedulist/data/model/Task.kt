package com.varsitycollege.schedulist.data.model

import java.time.OffsetDateTime
import java.util.Date

// Represents a task, compatible with Google Tasks API and app UI
data class Task(
    var id: String? = null, // The unique ID from Firestore.
    val title: String = "",
    val description: String? = null,
    val dueDate: Date = Date(),
    val energyLevel: EnergyLevel = EnergyLevel.LOW,
    val isCompleted: Boolean = false,
    val taskListId: String = "", // The ID of the TaskList this task belongs to.
    val userId: String = "" // To know which user this task belongs to.
)

enum class EnergyLevel {
    LOW,
    MEDIUM,
    HIGH
}

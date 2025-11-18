package com.varsitycollege.schedulist.data.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Task(
    val id: String = "",
    val googleTaskId: String = "", // Google Tasks API task ID
    val title: String = "",
    val description: String = "", // User-editable description from Google Tasks
    val subtasksText: String = "", // Read-only subtasks as bullet points
    val dueDate: Date? = null, // Nullable - not all tasks have due dates
    val isCompleted: Boolean = false,
    val taskListId: String = "", // Firestore task list ID
    val googleTaskListId: String = "", // Google Tasks API list ID
    val userId: String = "",
    val hasSubtasks: Boolean = false, // Flag to indicate if subtasks exist
    val position: String = "" // For ordering tasks within a list
) {
    @Exclude
    fun hasDueDate(): Boolean = dueDate != null
}
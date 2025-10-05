package com.varsitycollege.schedulist.data.model

// Represents a list of tasks, with a relationship to Task
 data class TaskList(
    var id: String? = null, // The unique ID from Firestore.
    val name: String = "",
    val userId: String = "" // To know which user created this list.
)


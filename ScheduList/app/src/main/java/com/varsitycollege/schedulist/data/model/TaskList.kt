package com.varsitycollege.schedulist.data.model

// Represents a list of tasks, with a relationship to Task
 data class TaskList(
    val id: String = "",
    val name: String = "",
    val tasks: List<Task> = emptyList()
)


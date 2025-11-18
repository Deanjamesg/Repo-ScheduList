package com.varsitycollege.schedulist.data.model

data class TaskList(
    val id: String = "",
    val googleTaskListId: String = "", // Google Tasks API list ID
    val name: String = "",
    val userId: String = "",
    val taskCount: Int = 0 // Number of tasks in list
)
package com.varsitycollege.schedulist.data.model

// SimpleItem for simplified task representation

data class SimpleItem(
    val taskListId: String,
    val taskId: String,
    val title: String,
    val status: Boolean // true if completed, false otherwise
)

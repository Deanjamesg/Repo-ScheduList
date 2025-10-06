package com.varsitycollege.schedulist.data.model

import java.util.Date

// Represents a calendar, compatible with Google Calendar API
data class Event(
    var id: String? = null,
    val title: String = "",
    val description: String? = null,
    val startTime: Date = Date(), // Combines the date and time from the pickers.
    val location: String? = null,
   // val attachmentUrl: String? = null,
    val userId: String = "" // To know which user this event belongs to.
)

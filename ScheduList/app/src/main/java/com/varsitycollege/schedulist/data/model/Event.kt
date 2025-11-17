package com.varsitycollege.schedulist.data.model

import java.util.Date

// Represents a calendar, compatible with Google Calendar API
data class Event(
    val id: String = "",
    val googleCalendarEventId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val userId: String = "" 
)

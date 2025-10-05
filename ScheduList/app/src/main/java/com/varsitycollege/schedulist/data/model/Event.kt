package com.varsitycollege.schedulist.data.model

import java.time.OffsetDateTime

// Represents a calendar event, compatible with Google Calendar API and app UI
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDateTime: OffsetDateTime? = null, // RFC3339 format
    val endDateTime: OffsetDateTime? = null,   // RFC3339 format
    val location: String? = null,
    val attachments: List<String> = emptyList() // URIs or file names
)

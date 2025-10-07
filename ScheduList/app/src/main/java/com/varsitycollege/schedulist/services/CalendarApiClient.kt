package com.varsitycollege.schedulist.services

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.Calendar as CalendarModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

class CalendarApiClient(
    private val context: Context,
    userEmail: String
) {
    private val TAG = "CalendarApiClient"
    private var calendarService: Calendar

    init {
        val account = Account(userEmail, "com.google")
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(CalendarScopes.CALENDAR)
        ).apply {
            selectedAccount = account
        }

        calendarService = Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ScheduList")
            .build()

        Log.d(TAG, "Google Calendar API client for account $userEmail initialized successfully.")
    }

    suspend fun getOrInsertScheduListCalendar(): CalendarListEntry? {
        return withContext(Dispatchers.IO) {
            try {
                // First, try to find the calendar
                val existingCalendar = findCalendarBySummary("ScheduList")
                if (existingCalendar != null) {
                    Log.d(TAG, "Found existing ScheduList calendar.")
                    return@withContext existingCalendar
                }

                Log.d(TAG, "Creating a new ScheduList calendar...")
                val newCalendar = CalendarModel().apply {
                    summary = "ScheduList"
                    description = "Events and tasks managed by the ScheduList app."
                    timeZone = TimeZone.getDefault().id
                }
                calendarService.calendars().insert(newCalendar).execute()
                Log.d(TAG, "Successfully created ScheduList calendar.")

                // After creating, find it again to get the full CalendarListEntry object
                return@withContext findCalendarBySummary("ScheduList")

            } catch (e: Exception) {
                Log.e(TAG, "Error getting or inserting ScheduList calendar", e)
                return@withContext null
            }
        }
    }

    suspend fun getAllCalendarEvents(calendarId: String): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                calendarService.events().list(calendarId).execute().items ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events for calendar $calendarId", e)
                emptyList()
            }
        }
    }

    suspend fun insertEvent(calendarId: String, summary: String, description: String?, location: String?, startTime: DateTime, endTime: DateTime): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val event = Event().apply {
                    this.summary = summary
                    this.description = description
                    this.location = location
                    this.start = EventDateTime().setDateTime(startTime).setTimeZone(TimeZone.getDefault().id)
                    this.end = EventDateTime().setDateTime(endTime).setTimeZone(TimeZone.getDefault().id)
                }
                calendarService.events().insert(calendarId, event).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting event into calendar $calendarId", e)
                null
            }
        }
    }

    suspend fun updateEvent(calendarId: String, eventId: String, updatedEvent: Event): Event? {
        return withContext(Dispatchers.IO) {
            try {
                calendarService.events().update(calendarId, eventId, updatedEvent).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event $eventId", e)
                null
            }
        }
    }

    suspend fun deleteEvent(calendarId: String, eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                calendarService.events().delete(calendarId, eventId).execute()
                true // Successful deletion returns no content
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting event $eventId", e)
                false
            }
        }
    }

    private fun findCalendarBySummary(summary: String): CalendarListEntry? {
        return try {
            calendarService.calendarList().list().execute().items?.find {
                it.summary == summary
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding calendar by summary", e)
            null
        }
    }
}


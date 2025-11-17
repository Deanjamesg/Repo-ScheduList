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

    // Cache the ScheduList calendar ID to avoid repeated lookups
    private var cachedScheduListCalendarId: String? = null

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

    // Get or create the ScheduList calendar and return its ID
    // Uses caching to avoid repeated API calls
    suspend fun ensureScheduListCalendar(): String? {
        // Return cached ID if available
        cachedScheduListCalendarId?.let {
            Log.d(TAG, "Using cached ScheduList calendar ID: $it")
            return it
        }

        return withContext(Dispatchers.IO) {
            try {
                // Try to find existing calendar
                val existingCalendar = findCalendarBySummary("ScheduList")
                if (existingCalendar != null) {
                    Log.d(TAG, "Found existing ScheduList calendar with ID: ${existingCalendar.id}")
                    cachedScheduListCalendarId = existingCalendar.id
                    return@withContext existingCalendar.id
                }

                // Create new calendar if it doesn't exist
                Log.d(TAG, "Creating a new ScheduList calendar...")
                val newCalendar = CalendarModel().apply {
                    summary = "ScheduList"
                    description = "Events and tasks managed by the ScheduList app."
                    timeZone = TimeZone.getDefault().id
                }
                val createdCalendar = calendarService.calendars().insert(newCalendar).execute()

                cachedScheduListCalendarId = createdCalendar.id
                Log.d(TAG, "Successfully created ScheduList calendar with ID: ${createdCalendar.id}")

                return@withContext createdCalendar.id

            } catch (e: Exception) {
                Log.e(TAG, "Error ensuring ScheduList calendar", e)
                return@withContext null
            }
        }
    }

    // Get all events from ScheduList calendar
    suspend fun getAllEvents(): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                val calendarId = ensureScheduListCalendar() ?: return@withContext emptyList()
                val events = calendarService.events()
                    .list(calendarId)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()

                events.items ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all events", e)
                emptyList()
            }
        }
    }

    // Get a specific event by ID
    suspend fun getEventById(eventId: String): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val calendarId = ensureScheduListCalendar() ?: return@withContext null
                calendarService.events().get(calendarId, eventId).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching event $eventId", e)
                null
            }
        }
    }

    // Get events filtered by start date - TODO: implement end time
    suspend fun getEventsByDateRange(startDate: DateTime, endDate: DateTime? = null): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                val calendarId = ensureScheduListCalendar() ?: return@withContext emptyList()

                val request = calendarService.events()
                    .list(calendarId)
                    .setTimeMin(startDate)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)

                endDate?.let { request.setTimeMax(it) }

                val events = request.execute()
                events.items ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events by date range", e)
                emptyList()
            }
        }
    }

    // Insert a new event into ScheduList calendar
    suspend fun insertEvent(
        summary: String,
        description: String? = null,
        location: String? = null,
        startTime: DateTime,
        endTime: DateTime
    ): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val calendarId = ensureScheduListCalendar() ?: return@withContext null

                val event = Event().apply {
                    this.summary = summary
                    this.description = description
                    this.location = location
                    this.start = EventDateTime()
                        .setDateTime(startTime)
                        .setTimeZone(TimeZone.getDefault().id)
                    this.end = EventDateTime()
                        .setDateTime(endTime)
                        .setTimeZone(TimeZone.getDefault().id)
                }

                val insertedEvent = calendarService.events().insert(calendarId, event).execute()
                Log.d(TAG, "Successfully inserted event: ${insertedEvent.id}")
                insertedEvent
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting event", e)
                null
            }
        }
    }

    // Update an existing event
    suspend fun updateEvent(eventId: String, updatedEvent: Event): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val calendarId = ensureScheduListCalendar() ?: return@withContext null
                val updated = calendarService.events().update(calendarId, eventId, updatedEvent).execute()
                Log.d(TAG, "Successfully updated event: $eventId")
                updated
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event $eventId", e)
                null
            }
        }
    }

    // Delete an event
    suspend fun deleteEvent(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val calendarId = ensureScheduListCalendar() ?: return@withContext false
                calendarService.events().delete(calendarId, eventId).execute()
                Log.d(TAG, "Successfully deleted event: $eventId")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting event $eventId", e)
                false
            }
        }
    }


    // Get all user calendars
    suspend fun getAllUserCalendars(): List<CalendarListEntry> {
        return withContext(Dispatchers.IO) {
            try {
                val calendarList = calendarService.calendarList().list().execute()
                calendarList.items ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user calendars", e)
                emptyList()
            }
        }
    }

    // Import events from another calendar into ScheduList
    suspend fun importEventsFromCalendar(sourceCalendarId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val scheduListCalendarId = ensureScheduListCalendar() ?: return@withContext 0

                val sourceEvents = calendarService.events()
                    .list(sourceCalendarId)
                    .setSingleEvents(true)
                    .execute()

                val events = sourceEvents.items ?: return@withContext 0
                var importedCount = 0

                events.forEach { event ->
                    try {
                        val newEvent = Event().apply {
                            summary = event.summary
                            description = event.description
                            location = event.location
                            start = event.start
                            end = event.end
                            recurrence = event.recurrence
                            reminders = event.reminders
                        }

                        calendarService.events().insert(scheduListCalendarId, newEvent).execute()
                        importedCount++
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to import event: ${event.summary}", e)
                    }
                }

                Log.d(TAG, "Successfully imported $importedCount events from calendar $sourceCalendarId")
                importedCount
            } catch (e: Exception) {
                Log.e(TAG, "Error importing events from calendar $sourceCalendarId", e)
                0
            }
        }
    }

    // Import events from all user calendars into ScheduList
    suspend fun importAllEvents(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val allCalendars = getAllUserCalendars()
                var totalImported = 0

                allCalendars.forEach { calendar ->
                    if (calendar.summary != "ScheduList") {
                        totalImported += importEventsFromCalendar(calendar.id)
                    }
                }

                Log.d(TAG, "Total events imported from all calendars: $totalImported")
                totalImported
            } catch (e: Exception) {
                Log.e(TAG, "Error importing all events", e)
                0
            }
        }
    }

    // Clear the cached calendar ID
    fun clearCache() {
        cachedScheduListCalendarId = null
        Log.d(TAG, "Calendar cache cleared")
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
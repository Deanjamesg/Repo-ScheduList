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
        // Create an Account object for the user's email
        val account = Account(userEmail, "com.google")

        // Build the credential object
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(CalendarScopes.CALENDAR)
        ).apply {
            selectedAccount = account
        }

        // Build the Google Calendar service object
        calendarService = Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ScheduList")
            .build()

        Log.d(TAG, "Google Calendar API client for account $userEmail initialized successfully.")
    }

    suspend fun insertScheduListCalendar() {
        withContext(Dispatchers.IO) {
            try {
                // Check if the "ScheduList" calendar already exists
                val existingCalendar = findCalendarBySummary("ScheduList")
                if (existingCalendar != null) {
                    Log.d(TAG, "ScheduList calendar already exists. No need to create a new one.")
                    return@withContext
                }
                Log.d(TAG, "Creating a new ScheduList calendar...")

                // Create a new calendar model
                val newCalendar = CalendarModel().apply {
                    summary = "ScheduList"
                    description = "Events and tasks managed by the ScheduList app."
                    timeZone = TimeZone.getDefault().id
                }

                // Insert the new calendar
                val createdCalendar = calendarService.calendars().insert(newCalendar).execute()

                Log.d(TAG, "Successfully created calendar: ${createdCalendar.summary} (ID: ${createdCalendar.id})")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting calendar", e)
            }
        }
    }

    suspend fun insertEvent(summary: String, description: String, location: String, startTime: DateTime, endTime: DateTime) {
        withContext(Dispatchers.IO) {
            try {
                // First, find the "ScheduList" calendar to get its ID
                val scheduListCalendar = findCalendarBySummary("ScheduList")
                if (scheduListCalendar == null) {
                    Log.e(TAG, "ScheduList calendar not found. Cannot insert event.")
                    return@withContext
                }

                val calendarId = scheduListCalendar.id
                Log.d(TAG, "Found ScheduList calendar with ID: $calendarId. Proceeding to insert event.")

                // Create a new Event object
                val event = Event().apply {
                    this.summary = summary
                    this.description = description
                    this.location = location

                    // Set the start time
                    val start = EventDateTime().apply {
                        dateTime = startTime
                        timeZone = TimeZone.getDefault().id // Use the device's default timezone
                    }
                    this.start = start

                    // Set the end time
                    val end = EventDateTime().apply {
                        dateTime = endTime
                        timeZone = TimeZone.getDefault().id
                    }
                    this.end = end
                }

                // Insert the event into the specified calendar
                val createdEvent = calendarService.events().insert(calendarId, event).execute()

                Log.d(TAG, "Event created successfully: ${createdEvent.summary}, Link: ${createdEvent.htmlLink}")

            } catch (e: Exception) {
                Log.e(TAG, "Error inserting event", e)
            }
        }
    }
    
    private fun findCalendarBySummary(summary: String): com.google.api.services.calendar.model.CalendarListEntry? {
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

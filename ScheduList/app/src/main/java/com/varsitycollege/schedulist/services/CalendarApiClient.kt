package com.varsitycollege.schedulist.services

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Calendar as CalendarModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

class CalendarApiClient(
    private val context: Context,
    userEmail: String
) {
    private val TAG = "CalendarApiClient: "
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

    suspend fun insertScheduListCalendar() {
        withContext(Dispatchers.IO) {
            try {
                val existingCalendar = findCalendarBySummary("ScheduList")
                if (existingCalendar != null) {
                    Log.d(TAG, "ScheduList calendar already exists. No need to create a new one.")
                    return@withContext
                }
                Log.d(TAG, "Creating a new ScheduList calendar...")

                val newCalendar = CalendarModel().apply {
                    summary = "ScheduList"
                    description = "Events and tasks managed by the ScheduList app."
                    timeZone = TimeZone.getDefault().id
                }

                val createdCalendar = calendarService.calendars().insert(newCalendar).execute()

                Log.d(TAG, "Successfully created calendar: ${createdCalendar.summary} (ID: ${createdCalendar.id})")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting calendar", e)
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


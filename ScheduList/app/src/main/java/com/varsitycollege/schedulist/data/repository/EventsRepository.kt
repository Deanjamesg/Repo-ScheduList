package com.varsitycollege.schedulist.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.services.ApiClients
import com.varsitycollege.schedulist.data.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.TimeZone

typealias GoogleEvent = com.google.api.services.calendar.model.Event

class EventsRepository {

    private val calendarApiClient = ApiClients.calendarApi!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "EventsRepository"

    private fun getUserEventsCollection() =
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated"))
            .collection("events")

    // Get all events from Firebase
    fun getEventsLiveData(): LiveData<List<Event>> {
        val liveData = MutableLiveData<List<Event>>()

        try {
            getUserEventsCollection()
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to events", error)
                        liveData.postValue(emptyList())
                        return@addSnapshotListener
                    }

                    val events = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Event::class.java)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing event: ${doc.id}", e)
                            null
                        }
                    } ?: emptyList()

                    Log.d(TAG, "Loaded ${events.size} events from Firebase")
                    liveData.postValue(events)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up events listener", e)
            liveData.postValue(emptyList())
        }

        return liveData
    }

    // Get all events from Firebase
    suspend fun getEvents(): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = getUserEventsCollection().get().await()
                val events = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing event: ${doc.id}", e)
                        null
                    }
                }
                Log.d(TAG, "Fetched ${events.size} events from Firebase")
                events
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events from Firebase", e)
                emptyList()
            }
        }
    }

    // Get a specific event by ID from Firebase
    suspend fun getEventById(eventId: String): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = getUserEventsCollection().document(eventId).get().await()
                doc.toObject(Event::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching event $eventId from Firebase", e)
                null
            }
        }
    }

    // Filter events by date range from Firebase
    suspend fun getEventsByDateRange(startDate: Date, endDate: Date? = null): List<Event> {
        return withContext(Dispatchers.IO) {
            try {
                var query = getUserEventsCollection()
                    .whereGreaterThanOrEqualTo("startTime", startDate)

                // Add a end date filter
                endDate?.let {
                    query = query.whereLessThanOrEqualTo("startTime", it)
                }

                val snapshot = query.get().await()
                val events = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                }

                Log.d(TAG, "Fetched ${events.size} events for date range from Firebase")
                events
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching events by date range from Firebase", e)
                emptyList()
            }
        }
    }

    // Add a new event - Saves to Firebase immediately, syncs to Google Calendar in background
    suspend fun addEvent(
        title: String,
        description: String?,
        startTime: Date,
        endTime: Date,
        location: String?
    ): Event? {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext null

                val tempEventId = getUserEventsCollection().document().id

                val appEvent = Event(
                    id = tempEventId,
                    googleCalendarEventId = "",
                    title = title,
                    description = description ?: "",
                    location = location ?: "",
                    startTime = startTime,
                    endTime = endTime,
                    userId = userId
                )

                Log.d(TAG, "Saving event to Firebase: $tempEventId")
                getUserEventsCollection()
                    .document(tempEventId)
                    .set(appEvent)
                    .await()

                Log.d(TAG, "✓ Event saved to Firebase")

                try {
                    Log.d(TAG, "Syncing to Google Calendar...")
                    val googleEvent = calendarApiClient.insertEvent(
                        summary = title,
                        description = description,
                        location = location,
                        startTime = DateTime(startTime),
                        endTime = DateTime(endTime)
                    )

                    if (googleEvent != null) {
                        getUserEventsCollection()
                            .document(tempEventId)
                            .update("googleCalendarEventId", googleEvent.id)
                            .await()

                        Log.d(TAG, "✓ Event synced to Google Calendar: ${googleEvent.id}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync to Google Calendar (event saved locally)", e)
                }

                appEvent
            } catch (e: Exception) {
                Log.e(TAG, "Error adding event", e)
                null
            }
        }
    }

     // Update an existing event
    suspend fun updateEvent(event: Event): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Updating event in Firebase: ${event.id}")
                getUserEventsCollection()
                    .document(event.id)
                    .set(event)
                    .await()

                Log.d(TAG, "✓ Event updated in Firebase")

                try {
                    if (event.googleCalendarEventId.isNotEmpty()) {
                        Log.d(TAG, "Syncing update to Google Calendar...")
                        val googleEvent = event.toGoogleEvent()
                        calendarApiClient.updateEvent(event.googleCalendarEventId, googleEvent)
                        Log.d(TAG, "✓ Event synced to Google Calendar")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync update to Google Calendar (updated locally)", e)
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event", e)
                false
            }
        }
    }

    // Delete an event
    suspend fun deleteEvent(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val event = getUserEventsCollection().document(eventId).get().await()
                    .toObject(Event::class.java)

                Log.d(TAG, "Deleting event from Firebase: $eventId")
                getUserEventsCollection()
                    .document(eventId)
                    .delete()
                    .await()

                Log.d(TAG, "✓ Event deleted from Firebase")

                try {
                    if (event?.googleCalendarEventId?.isNotEmpty() == true) {
                        Log.d(TAG, "Syncing deletion to Google Calendar...")
                        calendarApiClient.deleteEvent(event.googleCalendarEventId)
                        Log.d(TAG, "✓ Event deleted from Google Calendar")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete from Google Calendar (deleted locally)", e)
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting event", e)
                false
            }
        }
    }

    // Sync events from Google Calendar to Firebase
    // Call this when app goes online or on initial load
    suspend fun syncFromGoogleCalendar() {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting sync from Google Calendar...")

                val googleEvents = calendarApiClient.getAllEvents()
                Log.d(TAG, "Found ${googleEvents.size} events in Google Calendar")

                googleEvents.forEach { googleEvent ->
                    try {
                        val appEvent = googleEvent.toAppEvent() ?: return@forEach

                        getUserEventsCollection()
                            .document(appEvent.id)
                            .set(appEvent)
                            .await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync event: ${googleEvent.id}", e)
                    }
                }

                Log.d(TAG, "✓ Sync from Google Calendar complete")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing from Google Calendar", e)
            }
        }
    }

    // Import Events from all calendars
    suspend fun importAllEvents(): Int {
        return withContext(Dispatchers.IO) {
            try {
                calendarApiClient.importAllEvents()
            } catch (e: Exception) {
                0
            }
        }
    }

    // Extension functions for mapping
    private fun GoogleEvent.toAppEvent(): Event? {
        val startTimeMillis = this.start?.dateTime?.value ?: return null
        val userId = auth.currentUser?.uid ?: return null

        return Event(
            id = this.id,
            googleCalendarEventId = this.id,
            title = this.summary ?: "No Title",
            description = this.description ?: "",
            location = this.location ?: "",
            startTime = Date(startTimeMillis),
            endTime = this.end?.dateTime?.value?.let { Date(it) },
            userId = userId
        )
    }

    private fun Event.toGoogleEvent(): GoogleEvent {
        return GoogleEvent().apply {
            summary = this@toGoogleEvent.title
            description = this@toGoogleEvent.description
            location = this@toGoogleEvent.location
            start = EventDateTime()
                .setDateTime(DateTime(this@toGoogleEvent.startTime))
                .setTimeZone(TimeZone.getDefault().id)
            end = EventDateTime()
                .setDateTime(DateTime(this@toGoogleEvent.endTime ?: Date(this@toGoogleEvent.startTime.time + 3600_000)))
                .setTimeZone(TimeZone.getDefault().id)
        }
    }
}
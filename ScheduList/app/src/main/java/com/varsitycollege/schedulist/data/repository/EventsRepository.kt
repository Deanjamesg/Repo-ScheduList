package com.varsitycollege.schedulist.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime
import com.varsitycollege.schedulist.data.model.Event // Your app's data model
import com.varsitycollege.schedulist.services.CalendarApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.TimeZone

typealias GoogleEvent = com.google.api.services.calendar.model.Event

class EventsRepository(private val calendarApiClient: CalendarApiClient) {

    suspend fun getEvents(): LiveData<List<Event>> {
        val liveData = MutableLiveData<List<Event>>()
        withContext(Dispatchers.IO) {
            val scheduListCalendar = calendarApiClient.getOrInsertScheduListCalendar()

            if (scheduListCalendar?.id != null) {
                val googleEvents = calendarApiClient.getAllCalendarEvents(scheduListCalendar.id)

                val mappedEvents = googleEvents.mapNotNull { googleEvent ->
                    val startTimeMillis = googleEvent.start?.dateTime?.value
                    if (startTimeMillis != null) {
                        Event(
                            id = googleEvent.id,
                            title = googleEvent.summary ?: "No Title",
                            description = googleEvent.description ?: "",
                            location = googleEvent.location ?: "",
                            startTime = Date(startTimeMillis)
                        )
                    } else {
                        null // Ignore all-day events for now
                    }
                }
                liveData.postValue(mappedEvents)
            } else {
                liveData.postValue(emptyList())
            }
        }
        return liveData
    }

    suspend fun addEvent(title: String, description: String?, startTime: Date, location: String?): Event? {
        return withContext(Dispatchers.IO) {
            val scheduListCalendar = calendarApiClient.getOrInsertScheduListCalendar()
            val calendarId = scheduListCalendar?.id ?: return@withContext null

            // NOTE: Google Calendar requires an end time. Default to 1 hour after the start time.
            val endTime = DateTime(startTime.time + 3600_000)

            val googleEvent = calendarApiClient.insertEvent(
                calendarId = calendarId,
                summary = title,
                description = description,
                location = location ?: "",
                startTime = DateTime(startTime),
                endTime = endTime
            )

            return@withContext googleEvent?.let {
                Event(
                    id = it.id,
                    title = it.summary ?: "No Title",
                    description = it.description ?: "",
                    location = it.location ?: "",
                    startTime = Date(it.start.dateTime.value)
                )
            }
        }
    }

    suspend fun updateEvent(event: Event): Boolean {
        return withContext(Dispatchers.IO) {
            val scheduListCalendar = calendarApiClient.getOrInsertScheduListCalendar()
            val calendarId = scheduListCalendar?.id ?: return@withContext false

            val googleEventToUpdate = GoogleEvent().apply {
                summary = event.title
                description = event.description
                location = event.location
                start = EventDateTime()
                    .setDateTime(DateTime(event.startTime))
                    .setTimeZone(TimeZone.getDefault().id)
                // Defaulting end time to 1 hour after start
                end = EventDateTime()
                    .setDateTime(DateTime(event.startTime.time + 3600_000))
                    .setTimeZone(TimeZone.getDefault().id)
            }

            val updatedEvent = calendarApiClient.updateEvent(calendarId, event.id, googleEventToUpdate)
            return@withContext updatedEvent != null
        }
    }

    suspend fun deleteEvent(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val scheduListCalendar = calendarApiClient.getOrInsertScheduListCalendar()
            val calendarId = scheduListCalendar?.id ?: return@withContext false

            return@withContext calendarApiClient.deleteEvent(calendarId, eventId)
        }
    }
}


package com.varsitycollege.schedulist.ui.main.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.ui.adapter.EventListItem
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class EventFilter {
    TODAY, THIS_WEEK, THIS_MONTH, ALL
}

class EventsViewModel(private val repository: EventsRepository) : ViewModel() {

    private val _displayList = MutableLiveData<List<EventListItem>>()
    val displayList: LiveData<List<EventListItem>> = _displayList

    private var currentFilter = EventFilter.ALL
    private var allEvents: List<Event> = emptyList()

    init {
        repository.getEventsLiveData().observeForever { events ->
            allEvents = events
            applyFilter()
        }
    }

    fun setFilter(filter: EventFilter) {
        currentFilter = filter
        applyFilter()
    }

    private fun applyFilter() {
        val filteredEvents = when (currentFilter) {
            EventFilter.TODAY -> filterToday(allEvents)
            EventFilter.THIS_WEEK -> filterThisWeek(allEvents)
            EventFilter.THIS_MONTH -> filterThisMonth(allEvents)
            EventFilter.ALL -> allEvents
        }
        formatListForDayView(filteredEvents)
    }

    private fun filterToday(events: List<Event>): List<Event> {
        val calendar = Calendar.getInstance()
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return events.filter { event ->
            val eventStart = event.startTime
            val eventEnd = event.endTime ?: eventStart

            // Show event if:
            // 1. It starts today, OR
            // 2. It started before today but ends today or later (ongoing event)
            (eventStart.after(startOfDay) && eventStart.before(endOfDay)) ||
            eventStart == startOfDay ||
            (eventStart.before(startOfDay) && (eventEnd.after(startOfDay) || eventEnd == startOfDay))
        }
    }

    private fun filterThisWeek(events: List<Event>): List<Event> {
        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Next 7 days from today
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val endOfWeek = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return events.filter { event ->
            val eventStart = event.startTime
            val eventEnd = event.endTime ?: eventStart

            // Show event if:
            // 1. It starts within the next 7 days, OR
            // 2. It started before today but ends within the next 7 days (ongoing event)
            ((eventStart.after(startOfToday) || eventStart == startOfToday) && eventStart.before(endOfWeek)) ||
            (eventStart.before(startOfToday) && (eventEnd.after(startOfToday) || eventEnd == startOfToday))
        }
    }

    private fun filterThisMonth(events: List<Event>): List<Event> {
        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Next 30 days from today
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val endOfMonth = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return events.filter { event ->
            val eventStart = event.startTime
            val eventEnd = event.endTime ?: eventStart

            // Show event if:
            // 1. It starts within the next 30 days, OR
            // 2. It started before today but ends within the next 30 days (ongoing event)
            ((eventStart.after(startOfToday) || eventStart == startOfToday) && eventStart.before(endOfMonth)) ||
            (eventStart.before(startOfToday) && (eventEnd.after(startOfToday) || eventEnd == startOfToday))
        }
    }

    fun addEvent(
        title: String,
        description: String?,
        startTime: Date,
        endTime: Date,
        location: String?,
        reminderType: com.varsitycollege.schedulist.data.model.ReminderType = com.varsitycollege.schedulist.data.model.ReminderType.NONE
    ) {
        viewModelScope.launch {
            repository.addEvent(title, description, startTime, endTime, location, reminderType)
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }

    suspend fun getEventById(eventId: String): Event? {
        return repository.getEventById(eventId)
    }


    private fun formatListForDayView(events: List<Event>) {
        val formattedList = events.map { event ->
            EventListItem.DayEventItem(event)
        }
        _displayList.value = formattedList
    }
}
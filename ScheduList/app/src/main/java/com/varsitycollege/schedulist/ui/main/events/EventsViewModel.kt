package com.varsitycollege.schedulist.ui.main.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.ui.adapter.EventListItem
import kotlinx.coroutines.launch
import java.util.Date

class EventsViewModel(private val repository: EventsRepository) : ViewModel() {

    private val _displayList = MutableLiveData<List<EventListItem>>()
    val displayList: LiveData<List<EventListItem>> = _displayList

    init {
        repository.getEventsLiveData().observeForever { events ->
            formatListForDayView(events)
        }
    }

    fun addEvent(
        title: String,
        description: String?,
        startTime: Date,
        endTime: Date,
        location: String?
    ) {
        viewModelScope.launch {
            repository.addEvent(title, description, startTime, endTime, location)
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
package com.varsitycollege.schedulist.ui.main.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.ui.adapter.EventListItem // Correct import added

// This is the ViewModel. It gets the data from the repository and
// prepares it for our Fragment to display.

class EventsViewModel(private val repository: EventsRepository) : ViewModel() {

    // This is the final list that our Fragment will be watching for changes.
    private val _displayList = MutableLiveData<List<EventListItem>>()
    val displayList: LiveData<List<EventListItem>> = _displayList

    // Store the current list of events in memory
    private val currentEvents = mutableListOf<Event>()

    // This function tells the repository to start loading data.
    fun loadEvents(userId: String) {
        repository.getEvents(userId).observeForever { rawEventList ->
            currentEvents.clear()
            currentEvents.addAll(rawEventList)
            formatListForDayView(currentEvents)
        }
    }

    // Add a new event and update the display list
    fun addEvent(event: Event) {
        currentEvents.add(event)
        formatListForDayView(currentEvents)
    }

    // This takes the raw list and wraps each item in a 'DayEventItem'
    // so the adapter knows which layout to use.
    private fun formatListForDayView(events: List<Event>) {
        val formattedList = events.map { event ->
            EventListItem.DayEventItem(event)
        }
        _displayList.value = formattedList
    }

    // Get an Event by its ID
    fun getEventById(eventId: String?): Event? {
        return currentEvents.find { it.id == eventId }
    }
}
package com.varsitycollege.schedulist.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.varsitycollege.schedulist.data.model.Event
import java.util.Date

// This is our repository. Its only job is to get the event data.
// Later on, this is where we'll put the real Firebase API calls.

class EventsRepository {

    // This function provides our list of events for testing.
    fun getEvents(userId: String): LiveData<List<Event>> {
        val liveData = MutableLiveData<List<Event>>()

        val sampleEvents = listOf(
            Event(id = "e1", title = "Group Project Meeting", description = "Final review of the UI.", location = "Online", startTime = Date()),
            Event(id = "e2", title = "Study for Exam", description = "Chapter 4-6.", location = "Library", startTime = Date()),
            Event(id = "e3", title = "Submit Assignment", description = "Final submission for PROG7311.", location = "VC Online", startTime = Date())
        )
        liveData.value = sampleEvents
        return liveData
    }
}
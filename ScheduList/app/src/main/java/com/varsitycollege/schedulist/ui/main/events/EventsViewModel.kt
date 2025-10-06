package com.varsitycollege.schedulist.ui.main.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.data.model.Event

class EventsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    fun fetchEvents(userId: String) {
        eventsCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val events = snapshot.toObjects(Event::class.java)
                    _events.value = events
                }
            }
    }

    fun addEvent(event: Event) {
        eventsCollection.add(event)
    }

    fun updateEvent(eventId: String, event: Event) {
        eventsCollection.document(eventId).set(event)
    }

    fun deleteEvent(eventId: String) {
        eventsCollection.document(eventId).delete()
    }
}
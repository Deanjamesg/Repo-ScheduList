package com.varsitycollege.schedulist.ui.main.simplelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.ui.adapter.SimpleListItem

// This ViewModel is for our 'Simple List' screen. Its job is to
// fetch both tasks AND events, combine them, and then sort them by date.

class SimpleListViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // The final, combined list that we will show in the RecyclerView.
    private val _combinedList = MutableLiveData<List<SimpleListItem>>()
    val combinedList: LiveData<List<SimpleListItem>> = _combinedList

    fun loadAllItems(userId: String) {
        // We need to fetch from two different collections, which are two separate async calls.
        val tasksQuery = db.collection("tasks").whereEqualTo("userId", userId).get()
        val eventsQuery = db.collection("events").whereEqualTo("userId", userId).get()

        // Tasks.whenAllSuccess waits for both fetches to complete before running.
        Tasks.whenAllSuccess<QuerySnapshot>(tasksQuery, eventsQuery)
            .addOnSuccessListener { results ->
                val taskDocs = results[0]
                val eventDocs = results[1]

                val simpleItems = mutableListOf<SimpleListItem>()

                // Convert all Tasks to SimpleListItems
                taskDocs.toObjects(Task::class.java).forEach { task ->
                    simpleItems.add(SimpleListItem(id = task.id ?: "", title = task.title, date = task.dueDate))
                }

                // Convert all Events to SimpleListItems
                eventDocs.toObjects(Event::class.java).forEach { event ->
                    simpleItems.add(SimpleListItem(id = event.id ?: "", title = event.title, date = event.startTime))
                }

                // Sort the final combined list by date
                _combinedList.value = simpleItems.sortedBy { it.date }
            }
    }
}
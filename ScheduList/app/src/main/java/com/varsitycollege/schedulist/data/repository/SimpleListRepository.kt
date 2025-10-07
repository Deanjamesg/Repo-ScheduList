package com.varsitycollege.schedulist.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.varsitycollege.schedulist.ui.adapter.SimpleListItem
import com.varsitycollege.schedulist.data.model.EnergyLevel
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.data.model.Task
import java.util.Date

// This is our repository for the Simple List. Its main job is to fetch
// BOTH tasks and events, then combine them into a single, sorted list.

class SimpleListRepository {

    fun getAllItems(userId: String): LiveData<List<SimpleListItem>> {
        val liveData = MutableLiveData<List<SimpleListItem>>()
        val combinedList = mutableListOf<SimpleListItem>()

        // 1. Get the sample tasks and convert them to SimpleListItem objects.
        val sampleTasks = getSampleTasks()
        val taskItems = sampleTasks.map { task ->
            SimpleListItem(
                id = task.id ?: "",
                title = task.title,
                date = task.dueDate,
                isCompleted = task.isCompleted // Added this line
            )
        }
        combinedList.addAll(taskItems)

        // 2. Get the sample events and convert them to SimpleListItem objects.
        val sampleEvents = getSampleEvents()
        val eventItems = sampleEvents.map { event ->
            SimpleListItem(
                id = event.id ?: "",
                title = event.title,
                date = event.startTime,
                isCompleted = false // Added this line (events can't be "completed")
            )
        }
        combinedList.addAll(eventItems)

        // 3. Sort the final combined list by date and post it to our LiveData.
        liveData.value = combinedList.sortedBy { it.date }
        return liveData
    }

    // Helper function to provide fake task data.
    private fun getSampleTasks(): List<Task> {
        return listOf(
            Task(id = "t1", title = "Complete UI proposal", dueDate = Date(), energyLevel = EnergyLevel.HIGH, isCompleted = false),
            Task(id = "t2", title = "Buy groceries", dueDate = Date(), isCompleted = true)
        )
    }

    // Helper function to provide fake event data.
    private fun getSampleEvents(): List<Event> {
        return listOf(
            Event(id = "e1", title = "Group Project Meeting", startTime = Date()),
            Event(id = "e2", title = "Doctor's Appointment", startTime = Date())
        )
    }
}
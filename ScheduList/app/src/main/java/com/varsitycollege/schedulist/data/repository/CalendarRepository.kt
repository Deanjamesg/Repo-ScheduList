package com.varsitycollege.schedulist.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.data.model.Task
import java.util.Date

class CalendarRepository {

    fun getCalendarTasks(userId: String): LiveData<List<Task>> {
        val liveData = MutableLiveData<List<Task>>()

        // Creating some sample tasks to test with.
        val sampleTasks = listOf(
            Task(id = "c1", title = "Group Meeting", dueDate = Date()),
            Task(id = "c2", title = "Study for Exam", dueDate = Date()),
            Task(id = "c3", title = "Blood Tests", dueDate = Date()),
            Task(id = "c4", title = "Submit UI", dueDate = Date())
        )
        liveData.value = sampleTasks
        return liveData
    }
}
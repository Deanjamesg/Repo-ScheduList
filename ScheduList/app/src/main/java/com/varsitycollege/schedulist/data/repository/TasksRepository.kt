package com.varsitycollege.schedulist.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.varsitycollege.schedulist.data.model.EnergyLevel
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.services.TasksApiClient
import java.util.Date

class TasksRepository (private val tasksApiClient: TasksApiClient) {

    // This function provides our list of tasks.
    fun getTasks(userId: String): LiveData<List<Task>> {
        val liveData = MutableLiveData<List<Task>>()

        // Creating some sample tasks to test with.
        val sampleTasks = listOf(
            Task(id = "t1", title = "Complete UI for Schedulist", description = "Finish the Tasks and Events fragments.", dueDate = Date(), energyLevel = EnergyLevel.HIGH, taskListId = "work1"),
            Task(id = "t2", title = "Prepare for presentation", description = "Practice the demo flow.", dueDate = Date(), energyLevel = EnergyLevel.MEDIUM, taskListId = "work1"),
            Task(id = "t3", title = "Buy groceries", description = "Milk, bread, eggs.", dueDate = Date(), energyLevel = EnergyLevel.LOW, taskListId = "personal1")
        )
        liveData.value = sampleTasks
        return liveData
    }
}
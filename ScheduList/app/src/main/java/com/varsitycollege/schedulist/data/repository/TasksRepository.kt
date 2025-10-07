package com.varsitycollege.schedulist.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.varsitycollege.schedulist.data.model.EnergyLevel
import com.varsitycollege.schedulist.data.model.SimpleItem
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.model.TaskList
import com.varsitycollege.schedulist.services.TasksApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class TasksRepository (private val tasksApiClient: TasksApiClient) {

    suspend fun addTaskList(title: String): TaskList? {
        return withContext(Dispatchers.IO) {
            val createdGoogleList = tasksApiClient.insertTaskList(title)
            return@withContext createdGoogleList?.let {
                TaskList(
                    id = it.id,
                    name = it.title
                )
            }
        }
    }

    suspend fun addTask(taskListId: String, task: Task): Task? {
        return withContext(Dispatchers.IO) {
            val createdGoogleTask = tasksApiClient.insertTask(taskListId, task.title, task.description)
            return@withContext createdGoogleTask?.let {
                Task(
                    id = it.id,
                    title = it.title ?: "No Title",
                    description = it.notes ?: "",
                    isCompleted = it.status == "completed"
                )
            }
        }
    }

    suspend fun getTaskLists(): LiveData<List<TaskList>> {
        val liveData = MutableLiveData<List<TaskList>>()
        withContext(Dispatchers.IO) {
            val googleTaskLists = tasksApiClient.getAllTaskLists()
            val mappedLists = googleTaskLists.map { googleList ->
                TaskList(
                    id = googleList.id,
                    name = googleList.title
                )
            }
            liveData.postValue(mappedLists)
        }
        return liveData
    }

    suspend fun getTasksFromList(taskListId: String): LiveData<List<Task>> {
        val liveData = MutableLiveData<List<Task>>()
        withContext(Dispatchers.IO) {
            val googleTasks = tasksApiClient.getAllTasksFromList(taskListId)
            val mappedTasks = googleTasks.map { googleTask ->
                Task(
                    id = googleTask.id,
                    title = googleTask.title ?: "No Title",
                    description = googleTask.notes ?: "",
                    isCompleted = googleTask.status == "completed"
                )
            }
            liveData.postValue(mappedTasks)
        }
        return liveData
    }

//    fun getSimpleItems(userId: String): LiveData<List<SimpleItem>> {
//        return getTasks(userId).map { tasks: List<Task> ->
//            tasks.map { task: Task ->
//                SimpleItem(
//                    taskListId = task.taskListId,
//                    taskId = task.id ?: "",
//                    title = task.title,
//                    status = task.isCompleted
//                )
//            }
//        }
//    }
}
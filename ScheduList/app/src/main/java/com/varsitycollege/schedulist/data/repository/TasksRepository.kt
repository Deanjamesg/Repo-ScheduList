package com.varsitycollege.schedulist.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.type.DateTime
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

//    suspend fun addTask(taskListId: String, title: String?, description: String, dueData: Date): Task? {
//        return withContext(Dispatchers.IO) {
//            val createdGoogleTask = tasksApiClient.insertTask(taskListId, title, description, dueData)
//            return@withContext createdGoogleTask?.let {
//                Task(
//                    id = it.id,
//                    title = it.title ?: "No Title",
//                    description = it.notes ?: "",
//                    isCompleted = it.status == "completed"
//                )
//            }
//        }
//    }

    suspend fun addTask(title: String, notes: String?, dueDate: Date): Task? {
        return withContext(Dispatchers.IO) {
            val taskLists = tasksApiClient.getAllTaskLists()
            val scheduList = taskLists.find { it.title == "ScheduList Tasks" }

            if (scheduList != null) {
                val googleTask = tasksApiClient.insertTask(
                    taskListId = scheduList.id,
                    title = title,
                    notes = notes,
                    dueDate = com.google.api.client.util.DateTime(dueDate)
                )

                return@withContext googleTask?.let {
                    Task(
                        id = it.id,
                        title = it.title ?: "No Title",
                        description = it.notes ?: "",
                        isCompleted = it.status == "completed",
                        dueDate = it.due?.let { d -> Date(com.google.api.client.util.DateTime(d).value) } ?: Date()
                    )
                }
            } else {
                return@withContext null
            }
        }
    }

    suspend fun getTasks(): LiveData<List<Task>> {
        val liveData = MutableLiveData<List<Task>>()
        withContext(Dispatchers.IO) {
            val allTasks = mutableListOf<Task>()
            val googleTaskLists = tasksApiClient.getAllTaskLists()
            googleTaskLists.forEach { googleList ->
                val googleTasksInList = tasksApiClient.getAllTasksFromList(googleList.id)
                val mappedTasks = googleTasksInList.map { googleTask ->
                    Task(
                        id = googleTask.id,
                        title = googleTask.title ?: "No Title",
                        description = googleTask.notes ?: "",
                        isCompleted = googleTask.status == "completed"
                    )
                }
                allTasks.addAll(mappedTasks)
            }
            liveData.postValue(allTasks)
        }
        return liveData
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
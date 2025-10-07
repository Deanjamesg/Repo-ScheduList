package com.varsitycollege.schedulist.services

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.tasks.Tasks
import com.google.api.services.tasks.TasksScopes
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TasksApiClient(
    private val context: Context,
    userEmail: String
) {
    private val TAG = "TasksApiClient"
    private var tasksService: Tasks

    init {
        val account = Account(userEmail, "com.google")
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(TasksScopes.TASKS)
        ).apply {
            selectedAccount = account
        }

        tasksService = Tasks.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ScheduList")
            .build()

        Log.d(TAG, "Google Tasks API client for account $userEmail initialized successfully.")
    }

    suspend fun getAllTaskLists(): List<TaskList> {
        return withContext(Dispatchers.IO) {
            try {
                tasksService.tasklists().list().execute().items ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching task lists", e)
                emptyList()
            }
        }
    }

    suspend fun insertTaskList(title: String): TaskList? {
        return withContext(Dispatchers.IO) {
            try {
                val newTaskList = TaskList().apply {
                    this.title = title
                }
                tasksService.tasklists().insert(newTaskList).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting new task list", e)
                null
            }
        }
    }

    suspend fun getAllTasksFromList(taskListId: String): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                // Set showCompleted to true to get both completed and incomplete tasks
                tasksService.tasks().list(taskListId).setShowCompleted(true).execute().items ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks from list $taskListId", e)
                emptyList()
            }
        }
    }

    suspend fun insertTask(taskListId: String, title: String, notes: String? = null): Task? {
        return withContext(Dispatchers.IO) {
            try {
                val task = Task().apply {
                    this.title = title
                    if (notes != null) {
                        this.notes = notes
                    }
                }
                tasksService.tasks().insert(taskListId, task).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting task", e)
                null
            }
        }
    }

    suspend fun updateTask(taskListId: String, taskId: String, updatedTask: Task): Task? {
        return withContext(Dispatchers.IO) {
            try {
                tasksService.tasks().update(taskListId, taskId, updatedTask).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task $taskId", e)
                null
            }
        }
    }

    suspend fun deleteTask(taskListId: String, taskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                tasksService.tasks().delete(taskListId, taskId).execute()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task $taskId", e)
                false
            }
        }
    }
}


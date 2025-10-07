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
        // Create an Account object for the user's email
        val account = Account(userEmail, "com.google")

        // Build the credential object for Google Tasks API
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(TasksScopes.TASKS)
        ).apply {
            selectedAccount = account
        }

        // Build the Google Tasks service object
        tasksService = Tasks.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ScheduList")
            .build()

        Log.d(TAG, "Google Tasks API client for account $userEmail initialized successfully.")
    }

    suspend fun CreateScheduListTaskList(): TaskList? {
        return withContext(Dispatchers.IO) {
            try {
                // First, try to find the task list by its title
                val taskList = findTaskListByTitle("ScheduList")
                if (taskList != null) {
                    Log.d(TAG, "Found existing 'ScheduList' task list.")
                    return@withContext taskList // Return the existing list
                }
                // If not found, create a new one
                Log.d(TAG, "Creating new 'ScheduList' task list.")
                val newTaskList = TaskList().apply {
                    title = "ScheduList"
                }
                val createdTaskList = tasksService.tasklists().insert(newTaskList).execute()
                Log.d(TAG, "Successfully created task list: ${createdTaskList.title} (ID: ${createdTaskList.id})")
                return@withContext createdTaskList

            } catch (e: Exception) {
                Log.e(TAG, "Error getting or creating ScheduList task list", e)
                return@withContext null
            }
        }
    }

    suspend fun insertTask(taskListId: String, title: String, notes: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                val task = Task().apply {
                    this.title = title
                    if (notes != null) {
                        this.notes = notes
                    }
                }
                val createdTask = tasksService.tasks().insert(taskListId, task).execute()
                Log.d(TAG, "Successfully created task: ${createdTask.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting task", e)
            }
        }
    }

    private fun findTaskListByTitle(title: String): TaskList? {
        return try {
            // Retrieve all task lists and find the one with the matching title
            tasksService.tasklists().list().execute().items?.find {
                it.title == title
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding task list by title", e)
            null
        }
    }
}

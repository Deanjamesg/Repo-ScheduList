package com.varsitycollege.schedulist.services

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
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

    // Cache the ScheduList task list ID to avoid repeated lookups
    private var cachedScheduListTaskListId: String? = null

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

    suspend fun ensureScheduListTaskList(): String? {
        cachedScheduListTaskListId?.let {
            Log.d(TAG, "Using cached ScheduList task list ID: $it")
            return it
        }

        return withContext(Dispatchers.IO) {
            try {
                val existingList = findTaskListByTitle("ScheduList")
                if (existingList != null) {
                    Log.d(TAG, "Found existing ScheduList task list with ID: ${existingList.id}")
                    cachedScheduListTaskListId = existingList.id
                    return@withContext existingList.id
                }

                Log.d(TAG, "Creating a new ScheduList task list...")
                val newTaskList = TaskList().apply {
                    title = "ScheduList"
                }
                val createdList = tasksService.tasklists().insert(newTaskList).execute()

                cachedScheduListTaskListId = createdList.id
                Log.d(TAG, "Successfully created ScheduList task list with ID: ${createdList.id}")

                return@withContext createdList.id
            } catch (e: Exception) {
                Log.e(TAG, "Error ensuring ScheduList task list", e)
                return@withContext null
            }
        }
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

    suspend fun deleteTaskList(taskListId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                tasksService.tasklists().delete(taskListId).execute()
                Log.d(TAG, "Successfully deleted task list: $taskListId")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task list $taskListId", e)
                false
            }
        }
    }

    suspend fun getAllTasksFromList(taskListId: String): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val allTasks = tasksService.tasks()
                    .list(taskListId)
                    .setShowCompleted(true)
                    .setShowHidden(true)
                    .execute()
                    .items ?: emptyList()

                // Separate parent tasks and subtasks
                val parentTasks = allTasks.filter { it.parent == null }
                val subtasksMap = allTasks.filter { it.parent != null }.groupBy { it.parent }

                // Merge subtasks into parent task descriptions
                parentTasks.map { parentTask ->
                    val subtasks = subtasksMap[parentTask.id] ?: emptyList()

                    if (subtasks.isNotEmpty()) {
                        // Build subtasks bullet list
                        val subtasksBulletList = subtasks
                            .sortedBy { it.position }
                            .joinToString("\n") { "• ${it.title}" }

                        // Append to existing description or create new
                        val mergedDescription = if (parentTask.notes.isNullOrEmpty()) {
                            subtasksBulletList
                        } else {
                            "${parentTask.notes}\n\nSubtasks:\n$subtasksBulletList"
                        }

                        // Return modified task with merged description
                        parentTask.apply {
                            notes = mergedDescription
                        }
                    } else {
                        parentTask
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks from list $taskListId", e)
                emptyList()
            }
        }
    }

    suspend fun getTasksWithDueDates(taskListId: String): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val allTasks = getAllTasksFromList(taskListId)
                allTasks.filter { it.due != null && it.due.isNotEmpty() }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks with due dates", e)
                emptyList()
            }
        }
    }

    suspend fun getTasksWithoutDueDates(taskListId: String): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val allTasks = getAllTasksFromList(taskListId)
                allTasks.filter { it.due == null || it.due.isEmpty() }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks without due dates", e)
                emptyList()
            }
        }
    }

    suspend fun getTasksByDateRange(
        taskListId: String,
        startDate: DateTime,
        endDate: DateTime
    ): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val allTasks = getAllTasksFromList(taskListId)
                val startMillis = startDate.value
                val endMillis = endDate.value

                allTasks.filter { task ->
                    val dueString = task.due
                    if (dueString != null) {
                        try {
                            // Parse the RFC 3339 string to DateTime
                            val dueDateTime = DateTime.parseRfc3339(dueString)
                            val dueMillis = dueDateTime.value
                            dueMillis >= startMillis && dueMillis <= endMillis
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks by date range", e)
                emptyList()
            }
        }
    }

    suspend fun getTaskById(taskListId: String, taskId: String): Task? {
        return withContext(Dispatchers.IO) {
            try {
                tasksService.tasks().get(taskListId, taskId).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching task $taskId", e)
                null
            }
        }
    }

    suspend fun insertTask(
        taskListId: String,
        title: String,
        notes: String? = null,
        due: DateTime? = null
    ): Task? {
        return withContext(Dispatchers.IO) {
            try {
                val task = Task().apply {
                    this.title = title
                    this.notes = notes
                    if (due != null) {
                        this.due = due.toStringRfc3339()
                    }
                    this.due = dueDate.toStringRfc3339()
                }

                val insertedTask = tasksService.tasks().insert(taskListId, task).execute()
                Log.d(TAG, "Successfully inserted task: ${insertedTask.id}")
                insertedTask
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting task", e)
                null
            }
        }
    }

    suspend fun updateTask(taskListId: String, taskId: String, updatedTask: Task): Task? {
        return withContext(Dispatchers.IO) {
            try {
                val updated = tasksService.tasks().update(taskListId, taskId, updatedTask).execute()
                Log.d(TAG, "Successfully updated task: $taskId")
                updated
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
                Log.d(TAG, "Successfully deleted task: $taskId")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task $taskId", e)
                false
            }
        }
    }

    suspend fun completeTask(taskListId: String, taskId: String): Task? {
        return withContext(Dispatchers.IO) {
            try {
                val task = tasksService.tasks().get(taskListId, taskId).execute()
                task.status = "completed"
                task.completed = DateTime(System.currentTimeMillis()).toStringRfc3339()
                tasksService.tasks().update(taskListId, taskId, task).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error completing task $taskId", e)
                null
            }
        }
    }

    suspend fun uncompleteTask(taskListId: String, taskId: String): Task? {
        return withContext(Dispatchers.IO) {
            try {
                val task = tasksService.tasks().get(taskListId, taskId).execute()
                task.status = "needsAction"
                task.completed = null
                tasksService.tasks().update(taskListId, taskId, task).execute()
            } catch (e: Exception) {
                Log.e(TAG, "Error uncompleting task $taskId", e)
                null
            }
        }
    }

    fun clearCache() {
        cachedScheduListTaskListId = null
        Log.d(TAG, "Task list cache cleared")
    }

    private fun findTaskListByTitle(title: String): TaskList? {
        return try {
            tasksService.tasklists().list().execute().items?.find {
                it.title == title
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding task list by title", e)
            null
        }
    }
}
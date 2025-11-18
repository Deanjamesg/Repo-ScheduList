package com.varsitycollege.schedulist.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.DateTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.model.TaskList
import com.varsitycollege.schedulist.services.ApiClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

typealias GoogleTask = com.google.api.services.tasks.model.Task
typealias GoogleTaskList = com.google.api.services.tasks.model.TaskList

class TasksRepository {

    private val tasksApiClient = ApiClients.tasksApi!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "TasksRepository"

    private fun getUserTasksCollection() =
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated"))
            .collection("tasks")

    // Helper to get current user's task lists collection
    private fun getUserTaskListsCollection() =
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated"))
            .collection("taskLists")

    // TASK OPERATIONS

    // Get all tasks from Firebase (real-time updates)
    fun getTasksLiveData(): LiveData<List<Task>> {
        val liveData = MutableLiveData<List<Task>>()

        try {
            getUserTasksCollection()
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to tasks", error)
                        liveData.postValue(emptyList())
                        return@addSnapshotListener
                    }

                    val tasks = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Task::class.java)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing task: ${doc.id}", e)
                            null
                        }
                    } ?: emptyList()

                    Log.d(TAG, "Loaded ${tasks.size} tasks from Firebase")
                    liveData.postValue(tasks)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up tasks listener", e)
            liveData.postValue(emptyList())
        }

        return liveData
    }

    // Get tasks from a specific task list
    fun getTasksFromListLiveData(taskListId: String): LiveData<List<Task>> {
        val liveData = MutableLiveData<List<Task>>()

        try {
            getUserTasksCollection()
                .whereEqualTo("taskListId", taskListId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to tasks from list", error)
                        liveData.postValue(emptyList())
                        return@addSnapshotListener
                    }

                    val tasks = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Task::class.java)
                    } ?: emptyList()

                    Log.d(TAG, "Loaded ${tasks.size} tasks from list $taskListId")
                    liveData.postValue(tasks)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up task list listener", e)
            liveData.postValue(emptyList())
        }

        return liveData
    }

    // Get tasks with due dates
    suspend fun getTasksWithDueDates(): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = getUserTasksCollection()
                    .whereNotEqualTo("dueDate", null)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks with due dates", e)
                emptyList()
            }
        }
    }

    // Get tasks without due dates
    suspend fun getTasksWithoutDueDates(): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = getUserTasksCollection()
                    .whereEqualTo("dueDate", null)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks without due dates", e)
                emptyList()
            }
        }
    }

    // Get tasks by date range
    suspend fun getTasksByDateRange(startDate: Date, endDate: Date): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = getUserTasksCollection()
                    .whereGreaterThanOrEqualTo("dueDate", startDate)
                    .whereLessThanOrEqualTo("dueDate", endDate)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching tasks by date range", e)
                emptyList()
            }
        }
    }

    // Get a specific task by ID
    suspend fun getTaskById(taskId: String): Task? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = getUserTasksCollection().document(taskId).get().await()
                doc.toObject(Task::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching task $taskId", e)
                null
            }
        }
    }

    // Add a new task
    suspend fun addTask(
        title: String,
        description: String?,
        dueDate: Date?,
        taskListId: String
    ): Task? {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext null

                // Get the task list to retrieve Google Task List ID
                val taskList = getUserTaskListsCollection().document(taskListId).get().await()
                    .toObject(TaskList::class.java)
                val googleTaskListId = taskList?.googleTaskListId ?: return@withContext null

                // Create temporary ID
                val tempTaskId = getUserTasksCollection().document().id

                // Create app task
                val appTask = Task(
                    id = tempTaskId,
                    googleTaskId = "",
                    title = title,
                    description = description ?: "",
                    subtasksText = "",
                    dueDate = dueDate,
                    isCompleted = false,
                    taskListId = taskListId,
                    googleTaskListId = googleTaskListId,
                    userId = userId,
                    hasSubtasks = false
                )

                // Save to Firebase FIRST
                Log.d(TAG, "Saving task to Firebase: $tempTaskId")
                getUserTasksCollection()
                    .document(tempTaskId)
                    .set(appTask)
                    .await()

                Log.d(TAG, "Task saved to Firebase")

                // Sync to Google Tasks in background
                try {
                    Log.d(TAG, "Syncing to Google Tasks...")
                    val googleTask = tasksApiClient.insertTask(
                        taskListId = googleTaskListId,
                        title = title,
                        notes = description,
                        due = dueDate?.let { DateTime(it) }
                    )

                    if (googleTask != null) {
                        getUserTasksCollection()
                            .document(tempTaskId)
                            .update("googleTaskId", googleTask.id)
                            .await()

                        Log.d(TAG, "Task synced to Google Tasks: ${googleTask.id}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync to Google Tasks (task saved locally)", e)
                }

                appTask
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task", e)
                null
            }
        }
    }

    // Update an existing task
    suspend fun updateTask(task: Task): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Update in Firebase FIRST
                Log.d(TAG, "Updating task in Firebase: ${task.id}")
                getUserTasksCollection()
                    .document(task.id)
                    .set(task)
                    .await()

                Log.d(TAG, "Task updated in Firebase")

                // Sync to Google Tasks
                try {
                    if (task.googleTaskId.isNotEmpty() && task.googleTaskListId.isNotEmpty()) {
                        Log.d(TAG, "Syncing update to Google Tasks...")
                        val googleTask = task.toGoogleTask()
                        tasksApiClient.updateTask(task.googleTaskListId, task.googleTaskId, googleTask)
                        Log.d(TAG, "Task synced to Google Tasks")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync update to Google Tasks (updated locally)", e)
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task", e)
                false
            }
        }
    }

    // Delete a task
    suspend fun deleteTask(taskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val task = getUserTasksCollection().document(taskId).get().await()
                    .toObject(Task::class.java)

                // Delete from Firebase FIRST
                Log.d(TAG, "Deleting task from Firebase: $taskId")
                getUserTasksCollection()
                    .document(taskId)
                    .delete()
                    .await()

                Log.d(TAG, "Task deleted from Firebase")

                // Delete from Google Tasks
                try {
                    if (task?.googleTaskId?.isNotEmpty() == true && task.googleTaskListId.isNotEmpty()) {
                        Log.d(TAG, "Syncing deletion to Google Tasks...")
                        tasksApiClient.deleteTask(task.googleTaskListId, task.googleTaskId)
                        Log.d(TAG, "Task deleted from Google Tasks")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete from Google Tasks (deleted locally)", e)
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task", e)
                false
            }
        }
    }

    // Toggle task completion
    suspend fun toggleTaskCompletion(taskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val task = getUserTasksCollection().document(taskId).get().await()
                    .toObject(Task::class.java) ?: return@withContext false

                val updatedTask = task.copy(isCompleted = !task.isCompleted)
                updateTask(updatedTask)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling task completion", e)
                false
            }
        }
    }

    // TASK LIST OPERATIONS

    // Get all task lists (real-time updates)
    fun getTaskListsLiveData(): LiveData<List<TaskList>> {
        val liveData = MutableLiveData<List<TaskList>>()

        try {
            getUserTaskListsCollection()
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to task lists", error)
                        liveData.postValue(emptyList())
                        return@addSnapshotListener
                    }

                    val taskLists = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(TaskList::class.java)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing task list: ${doc.id}", e)
                            null
                        }
                    } ?: emptyList()

                    Log.d(TAG, "Loaded ${taskLists.size} task lists from Firebase")
                    liveData.postValue(taskLists)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up task lists listener", e)
            liveData.postValue(emptyList())
        }

        return liveData
    }

    // Add a new task list
    suspend fun addTaskList(name: String): TaskList? {
        return withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext null

                val tempListId = getUserTaskListsCollection().document().id

                val appTaskList = TaskList(
                    id = tempListId,
                    googleTaskListId = "",
                    name = name,
                    userId = userId,
                    taskCount = 0
                )

                // Save to Firebase FIRST
                Log.d(TAG, "Saving task list to Firebase: $tempListId")
                getUserTaskListsCollection()
                    .document(tempListId)
                    .set(appTaskList)
                    .await()

                Log.d(TAG, "Task list saved to Firebase")

                // Sync to Google Tasks
                try {
                    Log.d(TAG, "Syncing to Google Tasks...")
                    val googleTaskList = tasksApiClient.insertTaskList(name)

                    if (googleTaskList != null) {
                        getUserTaskListsCollection()
                            .document(tempListId)
                            .update("googleTaskListId", googleTaskList.id)
                            .await()

                        Log.d(TAG, "Task list synced to Google Tasks: ${googleTaskList.id}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync to Google Tasks (list saved locally)", e)
                }

                appTaskList
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task list", e)
                null
            }
        }
    }

    // Delete a task list and all its tasks
    suspend fun deleteTaskList(taskListId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val taskList = getUserTaskListsCollection().document(taskListId).get().await()
                    .toObject(TaskList::class.java)

                // Delete all tasks in this list from Firebase
                val tasksSnapshot = getUserTasksCollection()
                    .whereEqualTo("taskListId", taskListId)
                    .get()
                    .await()

                tasksSnapshot.documents.forEach { it.reference.delete().await() }

                // Delete task list from Firebase
                getUserTaskListsCollection()
                    .document(taskListId)
                    .delete()
                    .await()

                Log.d(TAG, "Task list and its tasks deleted from Firebase")

                // Delete from Google Tasks
                try {
                    if (taskList?.googleTaskListId?.isNotEmpty() == true) {
                        tasksApiClient.deleteTaskList(taskList.googleTaskListId)
                        Log.d(TAG, "Task list deleted from Google Tasks")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete from Google Tasks (deleted locally)", e)
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task list", e)
                false
            }
        }
    }

    // INITIAL SYNC

    // Initial sync: Fetch all task lists and tasks from Google Tasks
    suspend fun performInitialSync() {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "STARTING INITIAL SYNC ")

                // Ensure ScheduList exists in Google Tasks
                val scheduListId = tasksApiClient.ensureScheduListTaskList()

                // Get all task lists from Google Tasks
                val googleTaskLists = tasksApiClient.getAllTaskLists()

                // Log each task list
                googleTaskLists.forEach { list ->
                    Log.d(TAG, "  - ${list.title} (ID: ${list.id})")
                }

                // Sync each task list
                var totalTasksSynced = 0
                googleTaskLists.forEach { googleTaskList ->
                    try {
                        Log.d(TAG, "Processing task list: ${googleTaskList.title}")

                        // Check if this task list already exists in Firebase
                        val existingList = getUserTaskListsCollection()
                            .whereEqualTo("googleTaskListId", googleTaskList.id)
                            .get()
                            .await()

                        val firebaseTaskListId = if (existingList.isEmpty) {
                            // Create new task list in Firebase
                            val newListId = getUserTaskListsCollection().document().id
                            val appTaskList = googleTaskList.toAppTaskList(newListId)

                            getUserTaskListsCollection()
                                .document(newListId)
                                .set(appTaskList)
                                .await()

                            Log.d(TAG, "Created task list in Firebase: ${googleTaskList.title}")
                            newListId
                        } else {
                            // Task list already exists
                            val existingId = existingList.documents[0].id
                            Log.d(TAG, "Task list already exists: ${googleTaskList.title}")
                            existingId
                        }

                        // Sync all tasks from this task list
                        val googleTasks = tasksApiClient.getAllTasksFromList(googleTaskList.id)
                        Log.d(TAG, "Found ${googleTasks.size} tasks in list: ${googleTaskList.title}")

                        googleTasks.forEach { googleTask ->
                            try {
                                Log.d(TAG, "Processing task: ${googleTask.title}")

                                // Check if task already exists
                                val existingTask = getUserTasksCollection()
                                    .whereEqualTo("googleTaskId", googleTask.id)
                                    .get()
                                    .await()

                                if (existingTask.isEmpty) {
                                    val appTask = googleTask.toAppTask(
                                        googleTaskListId = googleTaskList.id,
                                        taskListId = firebaseTaskListId
                                    )

                                    if (appTask != null) {
                                        getUserTasksCollection()
                                            .document(appTask.id)
                                            .set(appTask)
                                            .await()

                                        totalTasksSynced++
                                        Log.d(TAG, "Task synced: ${googleTask.title}")
                                    } else {
                                        Log.e(TAG, "Failed to convert task: ${googleTask.title}")
                                    }
                                } else {
                                    Log.d(TAG, "Task already exists: ${googleTask.title}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to sync task: ${googleTask.title}", e)
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to sync task list: ${googleTaskList.title}", e)
                    }
                }

                Log.d(TAG, "SYNC COMPLETE: $totalTasksSynced NEW TASKS SYNCED")
            } catch (e: Exception) {
                Log.e(TAG, "INITIAL SYNC FAILED", e)
                throw e
            }
        }
    }

    // Sync tasks from Google Tasks to Firebase (for manual refresh)
    suspend fun syncFromGoogleTasks() {
        performInitialSync()
    }

    // Extension functions for mapping
    private fun GoogleTask.toAppTask(googleTaskListId: String, taskListId: String): Task? {
        val userId = auth.currentUser?.uid ?: return null

        // Parse the RFC 3339 string to Date
        val parsedDueDate = this.due?.let { dueString ->
            try {
                Date(DateTime.parseRfc3339(dueString).value)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing due date: $dueString", e)
                null
            }
        }

        // Separate description from subtasks
        val notes = this.notes ?: ""
        val hasSubtasks = notes.contains("\n\nSubtasks:\n")

        val (taskDescription, subtasksText) = if (hasSubtasks) {
            val parts = notes.split("\n\nSubtasks:\n", limit = 2)
            Pair(parts[0], parts.getOrNull(1) ?: "")
        } else {
            Pair(notes, "")
        }

        return Task(
            id = this.id,
            googleTaskId = this.id,
            title = this.title ?: "No Title",
            description = taskDescription,
            subtasksText = subtasksText,
            dueDate = parsedDueDate,
            isCompleted = this.status == "completed",
            taskListId = taskListId,
            googleTaskListId = googleTaskListId,
            userId = userId,
            hasSubtasks = hasSubtasks,
            position = this.position ?: ""
        )
    }

    private fun GoogleTaskList.toAppTaskList(firebaseId: String): TaskList {
        val userId = auth.currentUser?.uid ?: ""

        return TaskList(
            id = firebaseId,
            googleTaskListId = this.id,
            name = this.title ?: "Unnamed List",
            userId = userId,
            taskCount = 0
        )
    }

    private fun Task.toGoogleTask(): GoogleTask {
        return GoogleTask().apply {
            title = this@toGoogleTask.title
            // Sync the description back to Google Tasks
            notes = this@toGoogleTask.description
            // Convert Date to RFC String
            due = this@toGoogleTask.dueDate?.let { date ->
                DateTime(date).toStringRfc3339()
            }
            status = if (this@toGoogleTask.isCompleted) "completed" else "needsAction"
            position = this@toGoogleTask.position
        }
    }
}
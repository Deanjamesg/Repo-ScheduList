package com.varsitycollege.schedulist.services

import android.content.Context
import android.util.Log
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.data.repository.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object SyncManager {
    private val TAG = "SyncManager"

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var isInitialSyncComplete = false
    private var isSyncing = false

    private val syncCallbacks = mutableListOf<SyncCallback>()

    interface SyncCallback {
        fun onSyncStarted()
        fun onSyncProgress(message: String)
        fun onSyncComplete(success: Boolean, message: String)
    }

    fun registerCallback(callback: SyncCallback) {
        syncCallbacks.add(callback)
    }

    fun unregisterCallback(callback: SyncCallback) {
        syncCallbacks.remove(callback)
    }

    fun performInitialSync(context: Context) {
        if (isSyncing) {
            Log.w(TAG, "Sync already in progress")
            return
        }

        syncScope.launch {
            isSyncing = true
            notifyCallbacks { it.onSyncStarted() }

            try {
                Log.d(TAG, "STARTING INITIAL SYNC")

                // Sync Tasks
                notifyCallbacks { it.onSyncProgress("Syncing task lists...") }
                syncTaskLists()

                // Sync Events
                notifyCallbacks { it.onSyncProgress("Syncing calendar events...") }
                syncEvents()

                isInitialSyncComplete = true
                Log.d(TAG, "INITIAL SYNC COMPLETE")
                notifyCallbacks { it.onSyncComplete(true, "Sync completed successfully") }

            } catch (e: Exception) {
                Log.e(TAG, "Initial sync failed", e)
                notifyCallbacks { it.onSyncComplete(false, "Sync failed: ${e.message}") }
            } finally {
                isSyncing = false
            }
        }
    }

    fun performManualSync(context: Context) {
        if (isSyncing) {
            Log.w(TAG, "Sync already in progress")
            return
        }

        syncScope.launch {
            isSyncing = true
            notifyCallbacks { it.onSyncStarted() }

            try {
                Log.d(TAG, "STARTING MANUAL SYNC")

                // Sync Tasks
                notifyCallbacks { it.onSyncProgress("Syncing tasks...") }
                syncTaskLists()

                // Sync Events
                notifyCallbacks { it.onSyncProgress("Syncing events...") }
                syncEvents()

                Log.d(TAG, "MANUAL SYNC COMPLETE")
                notifyCallbacks { it.onSyncComplete(true, "Refresh complete") }

            } catch (e: Exception) {
                Log.e(TAG, "Manual sync failed", e)
                notifyCallbacks { it.onSyncComplete(false, "Refresh failed: ${e.message}") }
            } finally {
                isSyncing = false
            }
        }
    }


    private suspend fun syncTaskLists() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Syncing Task Lists")

            val tasksRepository = TasksRepository()
            tasksRepository.performInitialSync()

            Log.d(TAG, "Task lists synced successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync task lists", e)
            throw e
        }
    }

    private suspend fun syncEvents() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Syncing Calendar Events")

            val eventsRepository = EventsRepository()
//             eventsRepository.performInitialSync()

            Log.d(TAG, "Events synced successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync events", e)
            throw e
        }
    }

    fun isInitialSyncComplete(): Boolean = isInitialSyncComplete


    fun isSyncing(): Boolean = isSyncing

    fun reset() {
        isInitialSyncComplete = false
        isSyncing = false
        syncCallbacks.clear()
    }

    private fun notifyCallbacks(action: (SyncCallback) -> Unit) {
        syncCallbacks.forEach { callback ->
            try {
                action(callback)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying callback", e)
            }
        }
    }
}
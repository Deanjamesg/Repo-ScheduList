package com.varsitycollege.schedulist

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Application class for ScheduList
 * Initializes Firebase Firestore with offline persistence enabled
 */
class ScheduListApplication : Application() {

    private val TAG = "ScheduListApplication"

    override fun onCreate() {
        super.onCreate()

        // Enable Firestore offline persistence
        enableFirestoreOfflineMode()

        Log.d(TAG, "ScheduList Application initialized with offline mode enabled")
    }

    private fun enableFirestoreOfflineMode() {
        try {
            val firestore = FirebaseFirestore.getInstance()

            // Configure Firestore settings with persistent cache
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .build()
                )
                .build()

            firestore.firestoreSettings = settings

            Log.d(TAG, "Firestore offline persistence enabled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling Firestore offline persistence", e)
        }
    }
}


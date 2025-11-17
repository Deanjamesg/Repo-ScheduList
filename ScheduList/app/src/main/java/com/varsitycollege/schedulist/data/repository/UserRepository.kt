package com.varsitycollege.schedulist.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.data.model.User
import com.varsitycollege.schedulist.data.model.NotificationPreferences
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val TAG = "UserRepository"

    suspend fun addUserIfNotExists(
        firebaseUser: FirebaseUser,
        scheduListCalendarId: String?
    ): Boolean {
        return try {
            val userId = firebaseUser.uid

            Log.d(TAG, "addUserIfNotExists")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Email: ${firebaseUser.email}")
            Log.d(TAG, "Display Name: ${firebaseUser.displayName}")
            Log.d(TAG, "Calendar ID: $scheduListCalendarId")

            val userDocRef = usersCollection.document(userId)
            Log.d(TAG, "Document path: users/$userId")

            Log.d(TAG, "Checking if document exists...")
            val document = userDocRef.get().await()
            Log.d(TAG, "Document exists: ${document.exists()}")

            if (!document.exists()) {
                Log.d(TAG, "NEW USER - Creating document...")

                val user = User(
                    userId = userId,
                    displayName = firebaseUser.displayName ?: "Unknown",
                    email = firebaseUser.email ?: "",
                    profilePictureUrl = firebaseUser.photoUrl?.toString(),
                    scheduListCalendarId = scheduListCalendarId,
                    notificationPrefs = NotificationPreferences()
                )

                Log.d(TAG, "User object created: $user")
                Log.d(TAG, "Attempting to save to Firestore...")

                userDocRef.set(user).await()

                Log.d(TAG, "SUCCESS: User document created!")
                Log.d(TAG, "Go check Firebase Console: users/$userId")

            } else {
                Log.d(TAG, "EXISTING USER - Updating calendar ID...")

                if (scheduListCalendarId != null) {
                    userDocRef.update("scheduListCalendarId", scheduListCalendarId).await()
                    Log.d(TAG, "Calendar ID updated")
                } else {
                    Log.d(TAG, "No calendar ID to update")
                }
            }

            Log.d(TAG, "addUserIfNotExists (SUCCESS)")
            true

        } catch (e: Exception) {
            Log.e(TAG, "ERROR in addUserIfNotExists")
            Log.e(TAG, "Exception type: ${e::class.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            e.printStackTrace()
            Log.e(TAG, "addUserIfNotExists (FAILED)")
            false
        }
    }
}
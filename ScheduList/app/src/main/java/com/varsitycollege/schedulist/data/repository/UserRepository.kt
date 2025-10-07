package com.varsitycollege.schedulist.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.data.model.User
import com.varsitycollege.schedulist.data.model.NotificationPreferences

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun addUserIfNotExists(firebaseUser: FirebaseUser, onComplete: (Boolean, Exception?) -> Unit) {
        val userId = firebaseUser.uid
        val userDocRef = usersCollection.document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val user = User(
                    userId = userId,
                    displayName = firebaseUser.displayName,
                    email = firebaseUser.email,
                    profilePictureUrl = firebaseUser.photoUrl?.toString(),
                    notificationPrefs = NotificationPreferences() // default prefs
                )
                userDocRef.set(user)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e) }
            } else {
                onComplete(true, null) // User already exists
            }
        }.addOnFailureListener { e ->
            onComplete(false, e)
        }
    }
}
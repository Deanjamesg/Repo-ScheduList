package com.varsitycollege.schedulist.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.data.model.NotificationPreferences
import com.varsitycollege.schedulist.data.model.User

// This ViewModel handles the logic for our settings screen. It's responsible
// for fetching the user's current settings from Firestore
// and saving any changes they make to the switches.

class SettingsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // LiveData to hold the user's current notification preferences.
    // The Fragment will observe this to update the switch UI.
    private val _notificationPrefs = MutableLiveData<NotificationPreferences>()
    val notificationPrefs: LiveData<NotificationPreferences> = _notificationPrefs

    // We call this from the Fragment to load the user's settings.
    fun loadSettings(userId: String) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // We found the user's document, so we convert it to our User data class.
                    val user = document.toObject(User::class.java)
                    // Then we update the LiveData with their specific preferences.
                    _notificationPrefs.value = user?.notificationPrefs
                }
            }
            .addOnFailureListener {
                // TODO: Handle the error if we can't fetch the user's settings.
            }
    }

    // This gets called from the Fragment when the user leaves the screen or hits a save button.
    fun saveNotificationPreferences(userId: String, prefs: NotificationPreferences) {
        // Here we update only the 'notificationPrefs' field in the user's document
        // in Firestore. This is more efficient than saving the whole user object again.
        usersCollection.document(userId)
            .update("notificationPrefs", prefs)
            .addOnSuccessListener {
                // Success! The settings are saved.
            }
            .addOnFailureListener {
                // TODO: Handle the error if saving fails.
            }
    }
}
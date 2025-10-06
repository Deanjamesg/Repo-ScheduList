package com.varsitycollege.schedulist.ui.main.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

// The ViewModel for our dashboard. Right now it just prepares a
// welcome message for the user.

class DashboardViewModel : ViewModel() {

    private val _welcomeMessage = MutableLiveData<String>()
    val welcomeMessage: LiveData<String> = _welcomeMessage

    fun generateWelcomeMessage() {
        val userName = FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.first()
        if (userName != null) {
            _welcomeMessage.value = "Welcome, $userName!"
        } else {
            _welcomeMessage.value = "Welcome!"
        }
    }
}
package com.varsitycollege.schedulist.ui.main.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

// This ViewModel just handles the data for the account screen, like
// showing the user's name and email.

class AccountViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // LiveData to hold the user's display name.
    private val _displayName = MutableLiveData<String?>()
    val displayName: LiveData<String?> = _displayName

    // LiveData to hold the user's email.
    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    // We can call this from the Fragment to load the user's info.
    fun loadUserData() {
        val currentUser = auth.currentUser
        _displayName.value = currentUser?.displayName
        _email.value = currentUser?.email
    }
}
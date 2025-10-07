package com.varsitycollege.schedulist.ui.main.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.data.model.User
import com.varsitycollege.schedulist.data.repository.UserRepository

// This ViewModel just handles the data for the account screen, like
// showing the user's name and email.

class AccountViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // LiveData to hold the user's display name.
    private val _displayName = MutableLiveData<String?>()
    val displayName: LiveData<String?> = _displayName

    // LiveData to hold the user's email.
    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // We can call this from the Fragment to load the user's info.
    fun loadUserData() {
        val currentUser = auth.currentUser
        _displayName.value = currentUser?.displayName
        _email.value = currentUser?.email
        val userId = currentUser?.uid ?: return
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    _user.value = user
                }
            }
            .addOnFailureListener {
                _user.value = null
            }
    }
}

class AccountViewModelFactory(private var repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

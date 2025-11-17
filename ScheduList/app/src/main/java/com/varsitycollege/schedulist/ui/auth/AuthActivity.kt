package com.varsitycollege.schedulist.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.MainActivity
import com.varsitycollege.schedulist.data.repository.UserRepository
import com.varsitycollege.schedulist.databinding.ActivityAuthBinding
import com.varsitycollege.schedulist.services.ApiClients
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var authorizationClient: AuthorizationClient
    private lateinit var auth: FirebaseAuth
    private val TAG = "AuthActivity"

    override fun onStart() {
        super.onStart()
        if (googleAuthClient.isSignedIn()) {
            continueToMain()
//            lifecycleScope.launch {
//                googleAuthClient.signOut()
//            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        googleAuthClient = GoogleAuthClient(this)
        authorizationClient = AuthorizationClient(this)
        auth = Firebase.auth

        binding.btnSignInWithGoogle.setOnClickListener {
            lifecycleScope.launch {
                startGoogleSignInFlow()
            }
        }
//        lifecycleScope.launch {
//            googleAuthClient.signOut()
//        }
    }

    private suspend fun startGoogleSignInFlow() {

         binding.progressBar.visibility = View.VISIBLE

        val signInSuccessful = googleAuthClient.signIn()

        if (signInSuccessful) {
            Log.d(TAG, "Step 1: Firebase Authentication successful.")
            Toast.makeText(this, "Authentication Successful!", Toast.LENGTH_SHORT).show()
            requestApiPermissions()
        } else {
            Log.e(TAG, "Step 1: Firebase Authentication failed")
            Toast.makeText(this, "Authentication Failed. Please try again.", Toast.LENGTH_LONG).show()
             binding.progressBar.visibility = View.GONE
        }
    }

    private fun requestApiPermissions() {
        Log.d(TAG, "Step 2: Requesting API permissions...")
        Toast.makeText(this, "Requesting Authorization...", Toast.LENGTH_SHORT).show()

        authorizationClient.requestAuthorization(
            onAuthorizationSuccess = { authorizationResult ->
                Log.d(TAG, "Step 2: Authorization successful!")
                Toast.makeText(this, "Authorization successful!", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    val user = auth.currentUser

                    if (user == null) {
                        Log.e(TAG, "ERROR: auth.currentUser is NULL!")
                        Toast.makeText(this@AuthActivity, "No user found!", Toast.LENGTH_SHORT).show()
                        continueToMain()
                        return@launch
                    }

                    Log.d(TAG, "Current user found: ${user.email}, UID: ${user.uid}")

                    try {
                        Toast.makeText(this@AuthActivity, "Setting up your account...", Toast.LENGTH_SHORT).show()

                        Log.d(TAG, "Initializing API clients...")
                        ApiClients.initialize(this@AuthActivity, user.email!!)
                        Log.d(TAG, "API clients initialized: calendarApi=${ApiClients.calendarApi != null}")

                        Log.d(TAG, "Creating/finding ScheduList calendar...")
                        val calendarId = ApiClients.calendarApi?.ensureScheduListCalendar()
                        Log.d(TAG, "Calendar ID: $calendarId")

                        if (calendarId == null) {
                            Log.e(TAG, "ERROR: Calendar ID is null!")
                            Toast.makeText(this@AuthActivity, "Calendar setup failed", Toast.LENGTH_SHORT).show()
                            continueToMain()
                            return@launch
                        }

                        Log.d(TAG, "Saving user to Firestore...")
                        Log.d(TAG, "User details - UID: ${user.uid}, Email: ${user.email}, Name: ${user.displayName}")

                        val userRepository = UserRepository()
                        val success = userRepository.addUserIfNotExists(user, calendarId)

                        Log.d(TAG, "Firestore save result: $success")

                        if (success) {
                            Toast.makeText(this@AuthActivity, "Account setup complete!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AuthActivity, "ERROR: Failed to save user", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "ERROR in setup flow:", e)
                        e.printStackTrace()
                        Toast.makeText(this@AuthActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        Log.d(TAG, "Navigating to MainActivity...")
                        continueToMain()
                    }
                }
            },
            onAuthorizationFailure = { exception ->
                Log.e(TAG, "Step 2: Authorization failed", exception)
                exception.printStackTrace()
                Toast.makeText(
                    this,
                    "Authorization failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()

                binding.progressBar.visibility = View.GONE
            }
        )
    }

    private fun continueToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}


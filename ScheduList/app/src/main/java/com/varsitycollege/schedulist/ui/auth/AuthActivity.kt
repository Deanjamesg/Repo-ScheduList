package com.varsitycollege.schedulist.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.api.client.util.DateTime
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.MainActivity
import com.varsitycollege.schedulist.databinding.ActivityAuthBinding
import com.varsitycollege.schedulist.services.CalendarApiClient
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
                // Both steps are now complete!
                Log.d(TAG, "Step 2: Authorization successful!")
                Toast.makeText(this, "Authorization successful!", Toast.LENGTH_SHORT).show()

                Toast.makeText(this, "Trying to Create ScheduList Calendar", Toast.LENGTH_SHORT).show()

                // Test API, insert a new calendar into user's Google Calendar, "ScheduList"
                lifecycleScope.launch {
                    val calendarApiClient = CalendarApiClient(this@AuthActivity, auth.currentUser!!.email.toString())
                    calendarApiClient.getOrInsertScheduListCalendar()
                }
                // Navigate to the main part of the application
                continueToMain()
            },
            onAuthorizationFailure = { exception ->
                Log.e(TAG, "Step 2: Authorization failed", exception)
                // Explain to the user why the permissions are needed
                Toast.makeText(
                    this,
                    "Permissions are required to access calendar and task features. You can grant them later in settings.",
                    Toast.LENGTH_LONG
                ).show()

                // Even if they deny, we can still let them into the app,
                // but the features just won't work.
//                continueToMain()
            }
        )
    }

    private fun continueToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}


package com.varsitycollege.schedulist.ui.auth

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.MainActivity
import com.varsitycollege.schedulist.databinding.ActivityAuthBinding
import com.varsitycollege.schedulist.services.CalendarApiClient
import com.varsitycollege.schedulist.biometrics.BiometricHelper
import com.varsitycollege.schedulist.biometrics.preferences.BiometricPreferences
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var authorizationClient: AuthorizationClient
    private lateinit var auth: FirebaseAuth
    private lateinit var biometricHelper: BiometricHelper
    private lateinit var biometricPreferences: BiometricPreferences
    private val TAG = "AuthActivity"

    override fun onStart() {
        super.onStart()
        // Check if user is already signed in AND has biometric enabled
        if (googleAuthClient.isSignedIn()) {
            if (biometricPreferences.isBiometricEnabled()) {
                // User was previously logged in with biometrics - skip to main
                continueToMain()
            } else {
                // User is signed in but hasn't set up biometrics yet
                continueToMain()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        googleAuthClient = GoogleAuthClient(this)
        authorizationClient = AuthorizationClient(this)
        auth = Firebase.auth
        biometricHelper = BiometricHelper(this)
        biometricPreferences = BiometricPreferences(this)

        binding.btnSignInWithGoogle.setOnClickListener {
            lifecycleScope.launch {
                startGoogleSignInFlow()
            }
        }

//        lifecycleScope.launch {
//            googleAuthClient.signOut()
//        }
    }

    override fun onResume() {
        super.onResume()
        // Check if user enrolled fingerprint and wants to enable it
        if (biometricPreferences.isUserLoggedIn() &&
            !biometricPreferences.isBiometricEnabled()) {
            val status = biometricHelper.isBiometricAvailable()
            if (status == BiometricHelper.BiometricStatus.AVAILABLE) {
                // User just enrolled, offer to test it
                testBiometric()
            }
        }
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

                // Mark user as logged in
                biometricPreferences.setUserLoggedIn(true)

                // Offer biometric setup for first-time users
                offerBiometricSetup()
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

    private fun offerBiometricSetup() {
        // Check if this is first login (biometric not yet configured)
        if (biometricPreferences.isBiometricEnabled()) {
            // Already set up, just continue
            continueToMain()
            return
        }

        when (biometricHelper.isBiometricAvailable()) {
            BiometricHelper.BiometricStatus.AVAILABLE -> {
                showBiometricSetupDialog()
            }
            BiometricHelper.BiometricStatus.NOT_ENROLLED -> {
                showEnrollmentRequiredDialog()
            }
            else -> {
                // Biometric not available, go to main
                Log.d(TAG, "Biometric authentication not available on this device")
                continueToMain()
            }
        }
    }

    private fun showBiometricSetupDialog() {
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this)
            .setTitle("Enable Fingerprint?")
            .setMessage("Would you like to use your fingerprint for faster login next time?")
            .setPositiveButton("Enable") { _, _ ->
                testBiometric()
            }
            .setNegativeButton("Skip") { _, _ ->
                continueToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun testBiometric() {
        biometricHelper.authenticate(
            title = "Setup Fingerprint",
            subtitle = "Touch sensor to enable fingerprint login",
            negativeButtonText = "Cancel",
            onSuccess = {
                biometricPreferences.setBiometricEnabled(true)
                Log.d(TAG, "Biometric authentication enabled successfully")
                Toast.makeText(this, "Fingerprint enabled!", Toast.LENGTH_SHORT).show()
                continueToMain()
            },
            onError = { _, errorMessage ->
                Log.e(TAG, "Biometric setup error: $errorMessage")
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                continueToMain()
            },
            onFailed = {
                Log.d(TAG, "Biometric setup failed")
                Toast.makeText(this, "Setup failed. Try again later.", Toast.LENGTH_SHORT).show()
                continueToMain()
            }
        )
    }

    private fun showEnrollmentRequiredDialog() {
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this)
            .setTitle("Fingerprint Required")
            .setMessage("Please register a fingerprint in your device settings to enable this feature.")
            .setPositiveButton("Open Settings") { _, _ ->
                openBiometricEnrollment()
            }
            .setNegativeButton("Skip") { _, _ ->
                continueToMain()
            }
            .setCancelable(false)
            .show()
    }

    private fun openBiometricEnrollment() {
        try {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
            }
            startActivity(enrollIntent)
            Toast.makeText(this, "After enrolling, return to complete setup", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                Toast.makeText(this, "After enrolling, return to complete setup", Toast.LENGTH_LONG).show()
            } catch (ex: Exception) {
                Toast.makeText(
                    this,
                    "Please manually enable fingerprint in Settings > Security",
                    Toast.LENGTH_LONG
                ).show()
                continueToMain()
            }
        }
    }

    private fun continueToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

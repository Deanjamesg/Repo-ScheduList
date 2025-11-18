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
import com.varsitycollege.schedulist.data.repository.UserRepository
import com.varsitycollege.schedulist.databinding.ActivityAuthBinding
import com.varsitycollege.schedulist.services.ApiClients
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
        // If user is already signed in, require biometric auth or force setup
        if (googleAuthClient.isSignedIn()) {
//            continueToMain()
//            lifecycleScope.launch {
//                googleAuthClient.signOut()
//            }

            if (biometricPreferences.isBiometricEnabled()) {
                // Require biometric authentication before continuing
                binding.progressBar.visibility = View.VISIBLE
                biometricHelper.authenticate(
                    title = "Unlock ScheduList",
                    subtitle = "Confirm with fingerprint to continue",
                    negativeButtonText = "Cancel",
                    onSuccess = {
                        binding.progressBar.visibility = View.GONE
                        continueToMain()
                    },
                    onError = { _, errorMessage ->
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "Biometric auth error onStart: $errorMessage")
                        showAuthFailedDialog(errorMessage)
                    },
                    onFailed = {
                        binding.progressBar.visibility = View.GONE
                        Log.d(TAG, "Biometric auth failed onStart")
                        showAuthFailedDialog("Authentication failed. Try again or sign out.")
                    }
                )
            } else {
                // Signed in but biometric not configured yet -> force setup
                offerBiometricSetup()
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
        // After returning from enrollment, if user logged in but biometric not yet enabled,
        // check if enrollment completed and prompt to test/enable.
        if (biometricPreferences.isUserLoggedIn() &&
            !biometricPreferences.isBiometricEnabled()) {
            val status = biometricHelper.isBiometricAvailable()
            if (status == BiometricHelper.BiometricStatus.AVAILABLE) {
                // User just enrolled, require testing/enabling now
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

                // Mark user as logged in
                biometricPreferences.setUserLoggedIn(true)

                // Force biometric setup for first-time users
                offerBiometricSetup()
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
                // Do not continue to main until biometric is configured (or allow limited access if desired).
                binding.progressBar.visibility = View.GONE
            }
        )
    }

    private fun offerBiometricSetup() {
        // Do not allow skipping biometric setup. Decide flow based on availability.
        if (biometricPreferences.isBiometricEnabled()) {
            // Already set up
            continueToMain()
            return
        }

        when (biometricHelper.isBiometricAvailable()) {
            BiometricHelper.BiometricStatus.AVAILABLE -> {
                // Offer only to enable (no skip)
                showBiometricSetupDialog()
            }
            BiometricHelper.BiometricStatus.NOT_ENROLLED -> {
                // Force enrollment (no skip)
                showEnrollmentRequiredDialog()
            }
            else -> {
                // Biometric not available on device - cannot proceed
                Log.d(TAG, "Biometric authentication not available on this device")
                showBiometricUnavailableDialog()
            }
        }
    }

    private fun showBiometricSetupDialog() {
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this)
            .setTitle("Enable Fingerprint")
            .setMessage("To continue you must enable fingerprint login for this app. Touch enable to set it up now.")
            .setPositiveButton("Enable") { _, _ ->
                testBiometric()
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
                // If setup errors, force user back to enrollment flow (don't continue)
                offerBiometricSetup()
            },
            onFailed = {
                Log.d(TAG, "Biometric setup failed")
                Toast.makeText(this, "Setup failed. Try again.", Toast.LENGTH_SHORT).show()
                // retry by offering setup again
                offerBiometricSetup()
            }
        )
    }

    private fun showEnrollmentRequiredDialog() {
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this)
            .setTitle("Fingerprint Required")
            .setMessage("Please register a fingerprint in your device settings to enable this feature. This cannot be skipped.")
            .setPositiveButton("Open Settings") { _, _ ->
                openBiometricEnrollment()
            }
            .setNegativeButton("Sign Out") { _, _ ->
                lifecycleScope.launch {
                    performSignOut()
                }
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
                // Can't continue without enrollment - force sign out
                lifecycleScope.launch {
                    performSignOut()
                }
            }
        }
    }

    private fun showBiometricUnavailableDialog() {
        binding.progressBar.visibility = View.GONE

        AlertDialog.Builder(this)
            .setTitle("Biometric Not Available")
            .setMessage("This device does not support the required fingerprint authentication. The app requires fingerprint login and cannot continue on this device.")
            .setPositiveButton("Sign Out") { _, _ ->
                lifecycleScope.launch {
                    performSignOut()
                }
            }
            .setNegativeButton("Exit") { _, _ ->
                finishAffinity()
            }
            .setCancelable(false)
            .show()
    }

    private fun showAuthFailedDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Authentication Required")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ ->
                // Retry biometric auth
                binding.progressBar.visibility = View.VISIBLE
                biometricHelper.authenticate(
                    title = "Retry Fingerprint",
                    subtitle = "Touch sensor to continue",
                    negativeButtonText = "Cancel",
                    onSuccess = {
                        binding.progressBar.visibility = View.GONE
                        continueToMain()
                    },
                    onError = { _, errorMessage ->
                        binding.progressBar.visibility = View.GONE
                        showAuthFailedDialog(errorMessage)
                    },
                    onFailed = {
                        binding.progressBar.visibility = View.GONE
                        showAuthFailedDialog("Authentication failed. Try again or sign out.")
                    }
                )
            }
            .setNegativeButton("Sign Out") { _, _ ->
                lifecycleScope.launch {
                    performSignOut()
                }
            }
            .setCancelable(false)
            .show()
    }

    private suspend fun performSignOut() {
        try {
            googleAuthClient.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
        biometricPreferences.setUserLoggedIn(false)
        biometricPreferences.setBiometricEnabled(false)
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
        // Stay on AuthActivity to allow sign-in again
    }

    private fun continueToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

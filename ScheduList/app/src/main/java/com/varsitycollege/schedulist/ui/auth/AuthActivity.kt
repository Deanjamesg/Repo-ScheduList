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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.MainActivity
import com.varsitycollege.schedulist.biometrics.BiometricHelper
import com.varsitycollege.schedulist.biometrics.preferences.BiometricPreferences
import com.varsitycollege.schedulist.data.repository.UserRepository
import com.varsitycollege.schedulist.databinding.ActivityAuthBinding
import com.varsitycollege.schedulist.services.ApiClients
import com.varsitycollege.schedulist.services.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthActivity : AppCompatActivity() {

    // VIEW BINDING
    private lateinit var binding: ActivityAuthBinding

    // AUTHENTICATION
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var authorizationClient: AuthorizationClient
    private lateinit var auth: FirebaseAuth

    // BIOMETRICS
    private lateinit var biometricHelper: BiometricHelper
    private lateinit var biometricPreferences: BiometricPreferences

    // CONSTANTS
    private val TAG = "AuthActivity"

    // LIFECYCLE METHODS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
        initializeAuthClients()
        setupClickListeners()
    }

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

    override fun onResume() {
        super.onResume()
        checkBiometricEnrollmentStatus()
    }

    // INITIALIZATION

    private fun initializeView() {
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initializeAuthClients() {
        googleAuthClient = GoogleAuthClient(this)
        authorizationClient = AuthorizationClient(this)
        auth = Firebase.auth
        biometricHelper = BiometricHelper(this)
        biometricPreferences = BiometricPreferences(this)

        Log.d(TAG, "Auth clients initialized")
    }

    private fun setupClickListeners() {
        binding.btnSignInWithGoogle.setOnClickListener {
            lifecycleScope.launch {
                handleGoogleSignInClick()
            }
        }
    }

    // SESSION MANAGEMENT

    // Check if user is signed in
    private fun checkExistingSession() {
        if (!googleAuthClient.isSignedIn()) {
            Log.d(TAG, "No existing session found")
            showSignInUI()
            return
        }

        Log.d(TAG, "Existing session found")

        if (biometricPreferences.isBiometricEnabled()) {
            // Require biometric auth
            authenticateWithBiometric()
        } else {
            // Biometric not configured -> force setup
            offerBiometricSetup()
        }
    }

    // Check if user returned from biometric enrollment
    private fun checkBiometricEnrollmentStatus() {
        if (biometricPreferences.isUserLoggedIn() &&
            !biometricPreferences.isBiometricEnabled()) {

            val status = biometricHelper.isBiometricAvailable()
            if (status == BiometricHelper.BiometricStatus.AVAILABLE) {
                // User just enrolled, require testing/enabling now
                testBiometricSetup()
            }
        }
    }

    // GOOGLE SIGN-IN FLOW

    // Handle Google Sign-In button click
    private suspend fun handleGoogleSignInClick() {
        showProgress()
        performGoogleSignIn()
    }

    // Step 1: Perform Google Sign-In (Firebase Auth)
    private suspend fun performGoogleSignIn() {
        Log.d(TAG, "Step 1: Starting Google Sign-In...")

        val signInSuccessful = googleAuthClient.signIn()

        if (signInSuccessful) {
            Log.d(TAG, "Step 1 Complete: Firebase Authentication successful")
            showToast("Authentication Successful!")
            requestApiAuthorization()
        } else {
            Log.e(TAG, "Step 1 Failed: Firebase Authentication failed")
            showToast("Authentication Failed. Please try again.")
            hideProgress()
        }
    }

    // Step 2: Request API Permissions (Calendar & Tasks)
    private fun requestApiAuthorization() {
        Log.d(TAG, "Step 2: Requesting API permissions...")
        showToast("Requesting Authorization...")

        authorizationClient.requestAuthorization(
            onAuthorizationSuccess = { authorizationResult ->
                Log.d(TAG, "Step 2 Complete: Authorization successful")
                showToast("Authorization successful!")
                lifecycleScope.launch {
                    setupUserAccount()
                }
            },
            onAuthorizationFailure = { error ->
                Log.e(TAG, "Step 2 Failed: Authorization failed - $error")
                showToast("Authorization failed: $error")
                hideProgress()
            }
        )
    }

    // Step 3: Setup User Account (API Clients, Firestore, Sync)
    private suspend fun setupUserAccount() {
        val user = auth.currentUser

        if (user == null) {
            Log.e(TAG, "ERROR: No Firebase user found after authentication")
            showToast("Authentication error. Please try again.")
            hideProgress()
            return
        }

        Log.d(TAG, "Step 3: Setting up account for ${user.email}")
        showToast("Setting up your account...")

        try {
            // 3a. Initialize API clients
            if (!initializeApiClients(user)) return

            // 3b. Setup Calendar and Task Lists
            val (calendarId, taskListId) = setupGoogleServices() ?: return

            // 3c. Save user to Firestore
            if (!saveUserToFirestore(user, calendarId)) return

            // 3d. Mark setup as complete
            showToast("Account setup complete!")
            Log.d(TAG, "Step 3 Complete: Account setup successful")

            // Step 4: Navigate to main activity
            navigateToMainActivity()

            // Step 5: Start background sync
            startBackgroundSync()

        } catch (e: Exception) {
            Log.e(TAG, "Step 3 Failed: Account setup error", e)
            showToast("Setup error: ${e.message}")
            navigateToMainActivity() // Continue despite error
        }
    }

    // Step 3a: Initialize API Clients
    private suspend fun initializeApiClients(user: FirebaseUser): Boolean {
        Log.d(TAG, "Initializing API clients...")

        return withContext(Dispatchers.IO) {
            try {
                ApiClients.initialize(this@AuthActivity, user.email!!)

                val initialized = ApiClients.calendarApi != null && ApiClients.tasksApi != null

                if (initialized) {
                    Log.d(TAG, "API clients initialized successfully")
                } else {
                    Log.e(TAG, "Failed to initialize API clients")
                    withContext(Dispatchers.Main) {
                        showToast("Failed to initialize Google services")
                        navigateToMainActivity()
                    }
                }

                initialized
            } catch (e: Exception) {
                Log.e(TAG, "API client initialization error", e)
                withContext(Dispatchers.Main) {
                    showToast("Initialization error: ${e.message}")
                    navigateToMainActivity()
                }
                false
            }
        }
    }

    // Step 3b: Setup Google Calendar and Task Lists
    private suspend fun setupGoogleServices(): Pair<String, String>? {
        Log.d(TAG, "Setting up Google Calendar and Tasks...")

        return withContext(Dispatchers.IO) {
            try {
                // Create/find ScheduList calendar
                val calendarId = ApiClients.calendarApi?.ensureScheduListCalendar()
                Log.d(TAG, "Calendar ID: $calendarId")

                if (calendarId == null) {
                    Log.e(TAG, "Calendar setup failed")
                    withContext(Dispatchers.Main) {
                        showToast("Calendar setup failed")
                        navigateToMainActivity()
                    }
                    return@withContext null
                }

                // Create/find ScheduList task list
                val taskListId = ApiClients.tasksApi?.ensureScheduListTaskList()
                Log.d(TAG, "Task List ID: $taskListId")

                if (taskListId == null) {
                    Log.e(TAG, "Task list setup failed")
                    withContext(Dispatchers.Main) {
                        showToast("Task list setup failed")
                        navigateToMainActivity()
                    }
                    return@withContext null
                }

                Log.d(TAG, "Google services setup complete")
                Pair(calendarId, taskListId)

            } catch (e: Exception) {
                Log.e(TAG, "Google services setup error", e)
                withContext(Dispatchers.Main) {
                    showToast("Google services error: ${e.message}")
                    navigateToMainActivity()
                }
                null
            }
        }
    }

    // Step 3c: Save User to Firestore
    private suspend fun saveUserToFirestore(user: FirebaseUser, calendarId: String): Boolean {
        Log.d(TAG, "Saving user to Firestore...")
        Log.d(TAG, "User: ${user.email}, UID: ${user.uid}")

        return withContext(Dispatchers.IO) {
            try {
                val userRepository = UserRepository()
                val success = userRepository.addUserIfNotExists(user, calendarId)

                if (success) {
                    Log.d(TAG, "User saved to Firestore")
                } else {
                    Log.e(TAG, "Failed to save user to Firestore")
                    withContext(Dispatchers.Main) {
                        showToast("Failed to save user data")
                        navigateToMainActivity()
                    }
                }

                success
            } catch (e: Exception) {
                Log.e(TAG, "Firestore save error", e)
                withContext(Dispatchers.Main) {
                    showToast("Database error: ${e.message}")
                    navigateToMainActivity()
                }
                false
            }
        }
    }

    // Step 4: Navigate to MainActivity
    private fun navigateToMainActivity() {
        Log.d(TAG, "Step 4: Navigating to MainActivity...")

        // Mark user as logged in for biometric preferences
        biometricPreferences.setUserLoggedIn(true)

        // Check biometric setup requirement
        if (!biometricPreferences.isBiometricEnabled()) {
            offerBiometricSetup()
        } else {
            continueToMain()
        }
    }

    // Step 5: Start background data sync
    private fun startBackgroundSync() {
        Log.d(TAG, "Step 5: Starting background sync...")

        // Add callback to monitor sync
        SyncManager.registerCallback(object : SyncManager.SyncCallback {
            override fun onSyncStarted() {
                Log.d(TAG, "Sync started in background")
            }

            override fun onSyncProgress(message: String) {
                Log.d(TAG, "Sync progress: $message")
            }

            override fun onSyncComplete(success: Boolean, message: String) {
                Log.d(TAG, if (success) "Sync complete: $message" else "Sync failed: $message")
            }
        })

        // Start sync
        SyncManager.performInitialSync(this)
        Log.d(TAG, "Sync initiated")
    }
    // BIOMETRIC AUTHENTICATION

    // Authenticate with biometric for returning users
    private fun authenticateWithBiometric() {
        Log.d(TAG, "Requiring biometric authentication...")
        showProgress()

        biometricHelper.authenticate(
            title = "Unlock ScheduList",
            subtitle = "Confirm with fingerprint to continue",
            negativeButtonText = "Cancel",
            onSuccess = {
                Log.d(TAG, "Biometric authentication successful")
                hideProgress()
                continueToMain()

                // Force biometric setup and authentication for all sign-ins
                offerBiometricSetup()
            },
                // Force biometric setup and authentication for all sign-ins
                offerBiometricSetup()
            },
            onError = { _, errorMessage ->
                Log.e(TAG, "Biometric auth error: $errorMessage")
                hideProgress()
                showAuthFailedDialog(errorMessage)
            },
            onFailed = {
                Log.d(TAG, "Biometric authentication failed")
                hideProgress()
                showAuthFailedDialog("Authentication failed. Try again or sign out.")
            }
        )
    }

    // Offer biometric setup for new users
    private fun offerBiometricSetup() {
        // Always require biometric authentication before proceeding
        if (biometricPreferences.isBiometricEnabled()) {
            // Already set up - require authentication
            requireBiometricAuthentication()
            return
        }

        // Not set up yet - force setup based on availability
        when (biometricHelper.isBiometricAvailable()) {
            BiometricHelper.BiometricStatus.AVAILABLE -> {
                showBiometricSetupDialog()
            }
            BiometricHelper.BiometricStatus.NOT_ENROLLED -> {
                showEnrollmentRequiredDialog()
            }
            else -> {
                showBiometricUnavailableDialog()
            }
        }
    }

    private fun requireBiometricAuthentication() {
        binding.progressBar.visibility = View.VISIBLE
        biometricHelper.authenticate(
            title = "Verify Identity",
            subtitle = "Confirm with fingerprint to continue",
            negativeButtonText = "Cancel",
            onSuccess = {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "Biometric authentication successful")
                continueToMain()
            },
            onError = { _, errorMessage ->
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Biometric auth error: $errorMessage")
                showAuthFailedDialog(errorMessage)
            },
            onFailed = {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "Biometric auth failed")
                showAuthFailedDialog("Authentication failed. Try again or sign out.")
            }
        )
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
                Log.d(TAG, "Biometric authentication enabled")
                showToast("Fingerprint enabled!")
                continueToMain()
            },
            onError = { _, errorMessage ->
                Log.e(TAG, "Biometric setup error: $errorMessage")
                showToast(errorMessage)
                offerBiometricSetup()
            },
            onFailed = {
                Log.d(TAG, "Biometric setup failed")
                showToast("Setup failed. Try again.")
                offerBiometricSetup()
            }
        )
    }

    // DIALOGS

    private fun showBiometricSetupDialog() {
        hideProgress()

        AlertDialog.Builder(this)
            .setTitle("Enable Fingerprint")
            .setMessage("To continue you must enable fingerprint login for this app. Touch enable to set it up now.")
            .setPositiveButton("Enable") { _, _ ->
                testBiometricSetup()
            }
            .setCancelable(false)
            .show()
    }

    private fun showEnrollmentRequiredDialog() {
        hideProgress()

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

    private fun showBiometricUnavailableDialog() {
        hideProgress()

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
                authenticateWithBiometric()
            }
            .setNegativeButton("Sign Out") { _, _ ->
                lifecycleScope.launch {
                    performSignOut()
                }
            }
            .setCancelable(false)
            .show()
    }

    // BIOMETRIC ENROLLMENT

    private fun openBiometricEnrollment() {
        try {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
            }
            startActivity(enrollIntent)
            showToast("After enrolling, return to complete setup")
        } catch (e: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                showToast("After enrolling, return to complete setup")
            } catch (ex: Exception) {
                showToast("Please manually enable fingerprint in Settings > Security")
                lifecycleScope.launch {
                    performSignOut()
                }
            }
        }
    }

    // SIGN OUT

    private suspend fun performSignOut() {
        try {
            googleAuthClient.signOut()
            ApiClients.clear()
            biometricPreferences.setUserLoggedIn(false)
            biometricPreferences.setBiometricEnabled(false)

            Log.d(TAG, "Sign out successful")
            showToast("Signed out")
            showSignInUI()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
            showToast("Sign out error: ${e.message}")
        } finally {
            hideProgress()
        }
    }

    // NAVIGATION

    private fun continueToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // UI HELPERS

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSignInUI() {
        hideProgress()
        binding.btnSignInWithGoogle.isEnabled = true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
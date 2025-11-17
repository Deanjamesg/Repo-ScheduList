package com.varsitycollege.schedulist.biometrics

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.varsitycollege.schedulist.MainActivity
import com.varsitycollege.schedulist.biometrics.preferences.BiometricPreferences
import com.varsitycollege.schedulist.ui.auth.AuthActivity

class LauncherActivity : AppCompatActivity() {

    private lateinit var biometricPreferences: BiometricPreferences
    private lateinit var biometricHelper: BiometricHelper
    private var authAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricPreferences = BiometricPreferences(this)
        biometricHelper = BiometricHelper(this)

        when {
            !biometricPreferences.isUserLoggedIn() -> {
                // First time user - go to login
                navigateToLogin()
            }
            biometricPreferences.isBiometricEnabled() -> {
                // Returning user with biometrics enabled
                promptBiometricAuth()
            }
            else -> {
                // Logged in but biometrics disabled
                navigateToMain()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check again when returning from Settings
        if (biometricPreferences.isUserLoggedIn() &&
            biometricPreferences.isBiometricEnabled()) {
            val status = biometricHelper.isBiometricAvailable()
            if (status == BiometricHelper.BiometricStatus.AVAILABLE) {
                // User enrolled fingerprint, now authenticate
                showBiometricPrompt()
            }
        }
    }

    private fun promptBiometricAuth() {
        when (biometricHelper.isBiometricAvailable()) {
            BiometricHelper.BiometricStatus.AVAILABLE -> {
                showBiometricPrompt()
            }
            BiometricHelper.BiometricStatus.NOT_ENROLLED -> {
                showEnrollFingerprintDialog()
            }
            else -> {
                // Hardware not available, go to main
                navigateToMain()
            }
        }
    }

    private fun showBiometricPrompt() {
        biometricHelper.authenticate(
            title = "Unlock ScheduList",
            subtitle = "Use your fingerprint to continue",
            negativeButtonText = "Sign Out",
            onSuccess = {
                authAttempts = 0
                navigateToMain()
            },
            onError = { errorCode, errorMessage ->
                if (errorCode == 13) { // User canceled (negative button)
                    handleSignOut()
                } else {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            onFailed = {
                authAttempts++
                if (authAttempts >= 3) {
                    showRetryOrSignOutDialog()
                } else {
                    Toast.makeText(this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showRetryOrSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Authentication Failed")
            .setMessage("Too many failed attempts. Would you like to try again or sign out?")
            .setPositiveButton("Retry") { _, _ ->
                authAttempts = 0
                showBiometricPrompt()
            }
            .setNegativeButton("Sign Out") { _, _ ->
                handleSignOut()
            }
            .setCancelable(false)
            .show()
    }

    private fun showEnrollFingerprintDialog() {
        AlertDialog.Builder(this)
            .setTitle("Fingerprint Required")
            .setMessage("Please register a fingerprint in your device settings to unlock ScheduList.")
            .setPositiveButton("Open Settings") { _, _ ->
                openBiometricEnrollment()
            }
            .setNegativeButton("Sign Out") { _, _ ->
                handleSignOut()
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
        } catch (e: Exception) {
            // Fallback to security settings if biometric enrollment not available
            try {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            } catch (ex: Exception) {
                Toast.makeText(
                    this,
                    "Please manually enable fingerprint in Settings > Security",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleSignOut() {
        biometricPreferences.clearAll()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

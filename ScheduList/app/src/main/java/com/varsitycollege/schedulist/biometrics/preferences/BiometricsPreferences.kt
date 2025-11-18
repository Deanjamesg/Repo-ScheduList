package com.varsitycollege.schedulist.biometrics.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class BiometricPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)

    fun isBiometricEnabled(): Boolean =
        prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false).also {
            Log.d("BiometricPrefs", "isBiometricEnabled=$it")
        }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        Log.d("BiometricPrefs", "setBiometricEnabled=$enabled")
    }

    fun isUserLoggedIn(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false).also {
            Log.d("BiometricPrefs", "isUserLoggedIn=$it")
        }

    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
        Log.d("BiometricPrefs", "setUserLoggedIn=$loggedIn")
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        Log.d("BiometricPrefs", "clearAll called - preferences cleared")
    }

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LOGGED_IN = "user_logged_in"
    }
}

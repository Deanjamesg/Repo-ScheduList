package com.varsitycollege.schedulist.biometrics.preferences

import android.content.Context
import android.content.SharedPreferences

class BiometricPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)

    fun isBiometricEnabled(): Boolean =
        prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isUserLoggedIn(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false)

    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LOGGED_IN = "user_logged_in"
    }
}

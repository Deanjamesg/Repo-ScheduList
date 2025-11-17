package com.varsitycollege.schedulist.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlin.apply
import android.content.res.Configuration
import java.util.Locale


class SettingsPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("schedulist_settings", Context.MODE_PRIVATE)

    // Notification Settings
    fun getTravelTimeAlertsEnabled(): Boolean =
        sharedPreferences.getBoolean("travel_time_alerts", true)

    fun setTravelTimeAlertsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("travel_time_alerts", enabled).apply()
    }

    fun getTaskRemindersEnabled(): Boolean =
        sharedPreferences.getBoolean("task_reminders", true)

    fun setTaskRemindersEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("task_reminders", enabled).apply()
    }

    fun getEventRemindersEnabled(): Boolean =
        sharedPreferences.getBoolean("event_reminders", true)

    fun setEventRemindersEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("event_reminders", enabled).apply()
    }

    fun getProductivityAlertsEnabled(): Boolean =
        sharedPreferences.getBoolean("productivity_alerts", true)

    fun setProductivityAlertsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("productivity_alerts", enabled).apply()
    }

    // Profile Settings
    fun getUsername(): String =
        sharedPreferences.getString("username", "") ?: ""

    fun setUsername(username: String) {
        sharedPreferences.edit().putString("username", username).apply()
    }

    fun getEmail(): String =
        sharedPreferences.getString("email", "") ?: ""

    fun setEmail(email: String) {
        sharedPreferences.edit().putString("email", email).apply()
    }

    fun getProfilePicturePath(): String =
        sharedPreferences.getString("profile_picture", "") ?: ""

    fun setProfilePicturePath(path: String) {
        sharedPreferences.edit().putString("profile_picture", path).apply()
    }

    // Appearance Settings
    fun getDarkModeEnabled(): Boolean =
        sharedPreferences.getBoolean("dark_mode", false)

    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun getThemeColor(): Int =
        sharedPreferences.getInt("theme_color", 0xFF6200EE.toInt())

    fun setThemeColor(color: Int) {
        sharedPreferences.edit().putInt("theme_color", color).apply()
    }

    fun getFontSize(): Float =
        sharedPreferences.getFloat("font_size", 14f)

    fun setFontSize(size: Float) {
        sharedPreferences.edit().putFloat("font_size", size).apply()
    }

    // Privacy Settings
    fun getAnalyticsEnabled(): Boolean =
        sharedPreferences.getBoolean("analytics_enabled", true)

    fun setAnalyticsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("analytics_enabled", enabled).apply()
    }

    fun getLocationTrackingEnabled(): Boolean =
        sharedPreferences.getBoolean("location_tracking", true)

    fun setLocationTrackingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("location_tracking", enabled).apply()
    }

    fun getDataSyncEnabled(): Boolean =
        sharedPreferences.getBoolean("data_sync", true)

    fun setDataSyncEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("data_sync", enabled).apply()
    }

    // Clear all settings
    fun clearAllSettings() {
        sharedPreferences.edit().clear().apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode_enabled", false)
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode_enabled", enabled).apply()
    }

    companion object {
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_AFRIKAANS = "af"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_TASK_REMINDERS = "task_reminders"
        private const val KEY_EVENT_REMINDERS = "event_reminders"
        private const val KEY_PRODUCTIVITY_ALERTS = "productivity_alerts"

        private const val KEY_OFFLINE_MODE = "offline_mode"
    }

    fun getLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    fun setLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    }



    fun isTaskRemindersEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_TASK_REMINDERS, true)
    }

    fun setTaskReminders(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_TASK_REMINDERS, enabled).apply()
    }

    fun isEventRemindersEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_EVENT_REMINDERS, true)
    }

    fun setEventReminders(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_EVENT_REMINDERS, enabled).apply()
    }

    fun isProductivityAlertsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_PRODUCTIVITY_ALERTS, true)
    }

    fun setProductivityAlerts(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PRODUCTIVITY_ALERTS, enabled).apply()
    }

    fun isOfflineModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_OFFLINE_MODE, false)
    }

    fun setOfflineMode(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_OFFLINE_MODE, enabled)
            .apply()
    }

    fun applyLanguage(context: Context) {
        val locale = when (getLanguage()) {
            LANGUAGE_AFRIKAANS -> Locale("af")
            else -> Locale("en")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

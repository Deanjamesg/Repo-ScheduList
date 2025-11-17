package com.varsitycollege.schedulist.data.repository

import android.content.Context
import com.varsitycollege.schedulist.data.preferences.SettingsPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val preferencesManager = SettingsPreferencesManager(context)

    private val _travelTimeAlertsEnabled = MutableStateFlow(preferencesManager.getTravelTimeAlertsEnabled())
    val travelTimeAlertsEnabled: StateFlow<Boolean> = _travelTimeAlertsEnabled.asStateFlow()

    private val _taskRemindersEnabled = MutableStateFlow(preferencesManager.getTaskRemindersEnabled())
    val taskRemindersEnabled: StateFlow<Boolean> = _taskRemindersEnabled.asStateFlow()

    private val _eventRemindersEnabled = MutableStateFlow(preferencesManager.getEventRemindersEnabled())
    val eventRemindersEnabled: StateFlow<Boolean> = _eventRemindersEnabled.asStateFlow()

    private val _productivityAlertsEnabled = MutableStateFlow(preferencesManager.getProductivityAlertsEnabled())
    val productivityAlertsEnabled: StateFlow<Boolean> = _productivityAlertsEnabled.asStateFlow()

    private val _username = MutableStateFlow(preferencesManager.getUsername())
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow(preferencesManager.getEmail())
    val email: StateFlow<String> = _email.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(preferencesManager.getDarkModeEnabled())
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _themeColor = MutableStateFlow(preferencesManager.getThemeColor())
    val themeColor: StateFlow<Int> = _themeColor.asStateFlow()

    private val _fontSize = MutableStateFlow(preferencesManager.getFontSize())
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _analyticsEnabled = MutableStateFlow(preferencesManager.getAnalyticsEnabled())
    val analyticsEnabled: StateFlow<Boolean> = _analyticsEnabled.asStateFlow()

    private val _locationTrackingEnabled = MutableStateFlow(preferencesManager.getLocationTrackingEnabled())
    val locationTrackingEnabled: StateFlow<Boolean> = _locationTrackingEnabled.asStateFlow()

    private val _dataSyncEnabled = MutableStateFlow(preferencesManager.getDataSyncEnabled())
    val dataSyncEnabled: StateFlow<Boolean> = _dataSyncEnabled.asStateFlow()

    fun setTravelTimeAlertsEnabled(enabled: Boolean) {
        preferencesManager.setTravelTimeAlertsEnabled(enabled)
        _travelTimeAlertsEnabled.value = enabled
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        preferencesManager.setTaskRemindersEnabled(enabled)
        _taskRemindersEnabled.value = enabled
    }

    fun setEventRemindersEnabled(enabled: Boolean) {
        preferencesManager.setEventRemindersEnabled(enabled)
        _eventRemindersEnabled.value = enabled
    }

    fun setProductivityAlertsEnabled(enabled: Boolean) {
        preferencesManager.setProductivityAlertsEnabled(enabled)
        _productivityAlertsEnabled.value = enabled
    }

    fun setUsername(username: String) {
        preferencesManager.setUsername(username)
        _username.value = username
    }

    fun setEmail(email: String) {
        preferencesManager.setEmail(email)
        _email.value = email
    }

    fun setProfilePicturePath(path: String) {
        preferencesManager.setProfilePicturePath(path)
    }

    fun getProfilePicturePath(): String {
        return preferencesManager.getProfilePicturePath()
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        preferencesManager.setDarkModeEnabled(enabled)
        _darkModeEnabled.value = enabled
    }

    fun setThemeColor(color: Int) {
        preferencesManager.setThemeColor(color)
        _themeColor.value = color
    }

    fun setFontSize(size: Float) {
        preferencesManager.setFontSize(size)
        _fontSize.value = size
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        preferencesManager.setAnalyticsEnabled(enabled)
        _analyticsEnabled.value = enabled
    }

    fun setLocationTrackingEnabled(enabled: Boolean) {
        preferencesManager.setLocationTrackingEnabled(enabled)
        _locationTrackingEnabled.value = enabled
    }

    fun setDataSyncEnabled(enabled: Boolean) {
        preferencesManager.setDataSyncEnabled(enabled)
        _dataSyncEnabled.value = enabled
    }

    fun clearAllSettings() {
        preferencesManager.clearAllSettings()
        refreshAllFlows()
    }

    private fun refreshAllFlows() {
        _travelTimeAlertsEnabled.value = preferencesManager.getTravelTimeAlertsEnabled()
        _taskRemindersEnabled.value = preferencesManager.getTaskRemindersEnabled()
        _eventRemindersEnabled.value = preferencesManager.getEventRemindersEnabled()
        _productivityAlertsEnabled.value = preferencesManager.getProductivityAlertsEnabled()
        _username.value = preferencesManager.getUsername()
        _email.value = preferencesManager.getEmail()
        _darkModeEnabled.value = preferencesManager.getDarkModeEnabled()
        _themeColor.value = preferencesManager.getThemeColor()
        _fontSize.value = preferencesManager.getFontSize()
        _analyticsEnabled.value = preferencesManager.getAnalyticsEnabled()
        _locationTrackingEnabled.value = preferencesManager.getLocationTrackingEnabled()
        _dataSyncEnabled.value = preferencesManager.getDataSyncEnabled()
    }
}

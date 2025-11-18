package com.varsitycollege.schedulist.ui.main.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.varsitycollege.schedulist.data.preferences.SettingsPreferencesManager
import com.varsitycollege.schedulist.databinding.FragmentNotificationsBinding
import com.varsitycollege.schedulist.notifications.NotificationDebugHelper
import com.varsitycollege.schedulist.notifications.NotificationPermissionHelper

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsManager: SettingsPreferencesManager
    private val TAG = "NotificationsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsPreferencesManager(requireContext())

        // Load current settings
        binding.switchTaskReminders.isChecked = settingsManager.isTaskRemindersEnabled()
        binding.switchEventReminders.isChecked = settingsManager.isEventRemindersEnabled()
        binding.switchProductivityAlerts.isChecked = settingsManager.isProductivityAlertsEnabled()

        // Save settings on change
        binding.switchTaskReminders.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.setTaskReminders(isChecked)
        }

        binding.switchEventReminders.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.setEventReminders(isChecked)
        }

        binding.switchProductivityAlerts.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.setProductivityAlerts(isChecked)
        }

        // Test notification button
        binding.btnSendTestNotification.setOnClickListener {
            handleTestNotification()
        }
    }

    private fun handleTestNotification() {
        Log.d(TAG, "Test notification button clicked")

        // First check all permissions and settings
        val statusReport = NotificationDebugHelper.checkNotificationStatus(requireContext())
        Log.d(TAG, statusReport)

        // Check if we have all necessary permissions
        if (!NotificationPermissionHelper.canScheduleExactAlarms(requireContext())) {
            Toast.makeText(
                requireContext(),
                "⚠️ Missing 'Alarms & reminders' permission!\nOpening settings...",
                Toast.LENGTH_LONG
            ).show()
            NotificationPermissionHelper.requestExactAlarmPermission(requireActivity())
            return
        }

        if (!NotificationPermissionHelper.hasNotificationPermission(requireActivity())) {
            Toast.makeText(
                requireContext(),
                "⚠️ Missing notification permission!",
                Toast.LENGTH_SHORT
            ).show()
            NotificationPermissionHelper.requestNotificationPermission(requireActivity())
            return
        }

        // Send test notification
        NotificationDebugHelper.sendTestNotification(requireContext())

        Toast.makeText(
            requireContext(),
            "✅ Test notification sent! Check your notification panel.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

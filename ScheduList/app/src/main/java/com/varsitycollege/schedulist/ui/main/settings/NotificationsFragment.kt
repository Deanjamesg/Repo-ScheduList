package com.varsitycollege.schedulist.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.varsitycollege.schedulist.data.preferences.SettingsPreferencesManager
import com.varsitycollege.schedulist.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsManager: SettingsPreferencesManager

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

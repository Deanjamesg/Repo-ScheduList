package com.varsitycollege.schedulist.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.varsitycollege.schedulist.data.preferences.SettingsPreferencesManager
import com.varsitycollege.schedulist.databinding.FragmentOfflineBinding

class OfflineFragment : Fragment() {

    private var _binding: FragmentOfflineBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsManager: SettingsPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfflineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsPreferencesManager(requireContext())

        // Load current setting
        binding.switchOfflineMode.isChecked = settingsManager.isOfflineModeEnabled()

        // Save on change
        binding.switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.setOfflineMode(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

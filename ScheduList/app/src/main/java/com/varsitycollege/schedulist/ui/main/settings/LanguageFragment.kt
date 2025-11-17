package com.varsitycollege.schedulist.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.data.preferences.SettingsPreferencesManager
import com.varsitycollege.schedulist.databinding.FragmentLanguageBinding

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsManager: SettingsPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsPreferencesManager(requireContext())

        // Load current language
        when (settingsManager.getLanguage()) {
            SettingsPreferencesManager.LANGUAGE_ENGLISH -> binding.rbEnglish.isChecked = true
            SettingsPreferencesManager.LANGUAGE_AFRIKAANS -> binding.rbAfrikaans.isChecked = true
        }

        binding.btnSaveLanguage.setOnClickListener {
            val selectedLanguage = when (binding.rgLanguage.checkedRadioButtonId) {
                R.id.rbEnglish -> SettingsPreferencesManager.LANGUAGE_ENGLISH
                R.id.rbAfrikaans -> SettingsPreferencesManager.LANGUAGE_AFRIKAANS
                else -> SettingsPreferencesManager.LANGUAGE_ENGLISH
            }

            settingsManager.setLanguage(selectedLanguage)
            settingsManager.applyLanguage(requireContext())

            Toast.makeText(requireContext(), "Language saved", Toast.LENGTH_SHORT).show()

            // Recreate the activity to apply language change
            requireActivity().recreate()
        }
    } 

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

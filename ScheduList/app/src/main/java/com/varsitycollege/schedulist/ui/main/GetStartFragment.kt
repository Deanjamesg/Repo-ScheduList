package com.varsitycollege.schedulist.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.varsitycollege.schedulist.databinding.FragmentGetStartedBinding

class GetStartFragment : Fragment() {
    lateinit var binding : FragmentGetStartedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false)

        binding.btnContinueToDashboard.setOnClickListener {
            val direction = GetStartFragmentDirections.actionGetStartFragmentToDashboardFragment()
            findNavController().navigate(direction)
        }

        return binding.root
    }
}
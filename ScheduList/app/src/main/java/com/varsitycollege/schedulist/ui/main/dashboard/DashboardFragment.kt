package com.varsitycollege.schedulist.ui.main.dashboard

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    lateinit var binding : FragmentDashboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        binding.cvTasks.setOnClickListener {
            val direction = DashboardFragmentDirections.actionDashboardFragmentToTasksFragment()
            this.findNavController().navigate(direction)
        }

        binding.cvEvents.setOnClickListener {
            val direction = DashboardFragmentDirections.actionDashboardFragmentToEventsFragment()
            this.findNavController().navigate(direction)
        }

        binding.cvCalendar.setOnClickListener {
        }

        binding.cvSimpleList.setOnClickListener {
            val direction = DashboardFragmentDirections.actionDashboardFragmentToSimpleListFragment()
            this.findNavController().navigate(direction)
        }

        binding.cvSettings.setOnClickListener {
            val direction = DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment()
            this.findNavController().navigate(direction)
        }

        binding.cvAccount.setOnClickListener {
            val direction = DashboardFragmentDirections.actionDashboardFragmentToAccountFragment()
            this.findNavController().navigate(direction)
        }

        return binding.root
    }
}
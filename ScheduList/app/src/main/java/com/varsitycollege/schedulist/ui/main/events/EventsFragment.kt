package com.varsitycollege.schedulist.ui.main.events

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.varsitycollege.schedulist.R

class EventsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spinnerEventList = view.findViewById<android.widget.Spinner>(R.id.spinnerEventList)
        val spinnerViewType = view.findViewById<android.widget.Spinner>(R.id.spinnerViewType)

        val eventListItems = listOf("All Events", "Birthdays", "Meetings", "Reminders")
        val viewTypeItems = listOf("List View", "Calendar View")

        val eventListAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, eventListItems)
        eventListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEventList.adapter = eventListAdapter

        val viewTypeAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewTypeItems)
        viewTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerViewType.adapter = viewTypeAdapter
    }
}
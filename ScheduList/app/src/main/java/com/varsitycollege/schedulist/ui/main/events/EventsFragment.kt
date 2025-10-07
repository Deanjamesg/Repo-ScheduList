package com.varsitycollege.schedulist.ui.main.events

import EventsViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.databinding.FragmentEventsBinding
import com.varsitycollege.schedulist.ui.adapter.EventsAdapter

// This is our Fragment. It's the UI part of the screen.
// Its job is to set up the view, observe the ViewModel, and update the adapter.

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We create our Repository and the Factory first.
        val repository = EventsRepository()
        val factory = EventsViewModelFactory(repository)

        // Then, we use the Factory to get our ViewModel.
        eventsViewModel = ViewModelProvider(this, factory).get(EventsViewModel::class.java)

        // We set up our RecyclerView and the adapter.
        setupRecyclerView()

        // Here we start observing the 'displayList' from our ViewModel.
        // This code will run automatically when the data is ready.
        eventsViewModel.displayList.observe(viewLifecycleOwner) { eventList ->
            // When we get the new list, we give it to our adapter to display.
            eventsAdapter.submitList(eventList)
        }

        // Now we tell the ViewModel to start loading the data.
        eventsViewModel.loadEvents("sampleUserId")
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter()
        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val spinnerEventList = view.findViewById<android.widget.Spinner>(R.id.spinnerEventList)
//        val spinnerViewType = view.findViewById<android.widget.Spinner>(R.id.spinnerViewType)
//
//        val eventListItems = listOf("All Events", "Birthdays", "Meetings", "Reminders")
//        val viewTypeItems = listOf("List View", "Calendar View")
//
//        val eventListAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, eventListItems)
//        eventListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerEventList.adapter = eventListAdapter
//
//        val viewTypeAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewTypeItems)
//        viewTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerViewType.adapter = viewTypeAdapter
 //   }
}
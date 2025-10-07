package com.varsitycollege.schedulist.ui.main.events

import EventsViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsitycollege.schedulist.R
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

        // Spinner setup
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

        // Overlay logic for Add Event
        val addEventOverlay = binding.root.findViewById<android.widget.FrameLayout>(R.id.addEventOverlay)
        val inflater = LayoutInflater.from(requireContext())
        var addEventView: View? = null

        binding.btnNewEvent.setOnClickListener {
            // Always remove previous view to reset form
            addEventOverlay.removeAllViews()
            addEventView = inflater.inflate(R.layout.fragment_add_event, addEventOverlay, false)
            addEventOverlay.addView(addEventView)

            // Get references to EditTexts inside TextInputLayouts
            val tilTitle = addEventView!!.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilTitle)
            val tilDescription = addEventView!!.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilDescription)
            val tilLocation = addEventView!!.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLocation)
            val etTitle = tilTitle.editText
            val etDescription = tilDescription.editText
            val etLocation = tilLocation.editText
            etTitle?.setText("")
            etDescription?.setText("")
            etLocation?.setText("")

            // Date and time pickers
            val btnDatePicker = addEventView!!.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDatePicker)
            val btnTimePicker = addEventView!!.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnTimePicker)
            val calendar = java.util.Calendar.getInstance()

            // Set initial button text
            val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            btnDatePicker.text = dateFormat.format(calendar.time)
            btnTimePicker.text = timeFormat.format(calendar.time)

            btnDatePicker.setOnClickListener {
                val context = requireContext()
                val datePicker = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(java.util.Calendar.YEAR, year)
                        calendar.set(java.util.Calendar.MONTH, month)
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                        btnDatePicker.text = dateFormat.format(calendar.time)
                    },
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                )
                // Set background and text color for DatePicker
                datePicker.setOnShowListener {
                    val dp = datePicker
                    dp.window?.setBackgroundDrawableResource(android.R.color.white)
                    val decorView = dp.window?.decorView
                    decorView?.let {
                        // Try to set text color for all NumberPickers and TextViews
                        val queue = ArrayDeque<View>()
                        queue.add(it)
                        while (queue.isNotEmpty()) {
                            val v = queue.removeFirst()
                            if (v is android.widget.TextView) {
                                v.setTextColor(android.graphics.Color.BLACK)
                            } else if (v is android.view.ViewGroup) {
                                for (i in 0 until v.childCount) queue.add(v.getChildAt(i))
                            }
                        }
                    }
                }
                datePicker.show()
            }

            btnTimePicker.setOnClickListener {
                val context = requireContext()
                val timePicker = android.app.TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(java.util.Calendar.MINUTE, minute)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        btnTimePicker.text = timeFormat.format(calendar.time)
                    },
                    calendar.get(java.util.Calendar.HOUR_OF_DAY),
                    calendar.get(java.util.Calendar.MINUTE),
                    false
                )
                // Set background and text color for TimePicker
                timePicker.setOnShowListener {
                    val tp = timePicker
                    tp.window?.setBackgroundDrawableResource(android.R.color.white)
                    val decorView = tp.window?.decorView
                    decorView?.let {
                        val queue = ArrayDeque<View>()
                        queue.add(it)
                        while (queue.isNotEmpty()) {
                            val v = queue.removeFirst()
                            if (v is android.widget.TextView) {
                                v.setTextColor(android.graphics.Color.BLACK)
                            } else if (v is android.view.ViewGroup) {
                                for (i in 0 until v.childCount) queue.add(v.getChildAt(i))
                            }
                        }
                    }
                }
                timePicker.show()
            }

            val btnCancel = addEventView!!.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
            val btnSave = addEventView!!.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveEvent)
            btnCancel.setOnClickListener {
                addEventOverlay.visibility = View.GONE
            }
            btnSave.setOnClickListener {
                val title = etTitle?.text?.toString()?.trim() ?: ""
                val description = etDescription?.text?.toString()?.trim()
                val location = etLocation?.text?.toString()?.trim()
                val startTime = calendar.time
                val newEvent = com.varsitycollege.schedulist.data.model.Event(
                    title = title,
                    description = description,
                    startTime = startTime,
                    location = location,
                    userId = "sampleUserId"
                )
                eventsViewModel.addEvent(newEvent)
                addEventOverlay.visibility = View.GONE
            }
            addEventOverlay.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter(onDayEventClick = { event ->
            val bundle = Bundle().apply {
                putString("title", event.title)
                putString("description", event.description)
                putString("location", event.location)
                putLong("date", event.startTime?.time ?: 0L)
                putLong("time", event.startTime?.time ?: 0L)
            }
            try {
                findNavController().navigate(R.id.action_eventsFragment_to_eventViewFragment, bundle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
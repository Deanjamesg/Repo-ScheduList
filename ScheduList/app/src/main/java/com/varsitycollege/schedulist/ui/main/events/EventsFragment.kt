package com.varsitycollege.schedulist.ui.main.events

import EventsViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.databinding.FragmentEventsBinding
import com.varsitycollege.schedulist.services.CalendarApiClient
import com.varsitycollege.schedulist.ui.adapter.EventsAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

// This is our Fragment. It's the UI part of the screen.
// Its job is to set up the view, observe the ViewModel, and update the adapter.

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var eventsAdapter: EventsAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var calendarApiClient: CalendarApiClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        calendarApiClient = CalendarApiClient(requireContext(), auth.currentUser!!.email.toString())

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

        // Set up filter spinner
        val filterAdapter = android.widget.ArrayAdapter.createFromResource(
            requireContext(),
            R.array.event_filter_array,
            android.R.layout.simple_spinner_item
        )
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterSpinner.adapter = filterAdapter

        // Set default selection to "All" (position 0)
        binding.filterSpinner.setSelection(0)

        binding.filterSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                when (position) {
                    0 -> eventsViewModel.setFilter(EventFilter.ALL)
                    1 -> eventsViewModel.setFilter(EventFilter.TODAY)
                    2 -> eventsViewModel.setFilter(EventFilter.THIS_WEEK)
                    3 -> eventsViewModel.setFilter(EventFilter.THIS_MONTH)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Do nothing
            }
        }

        // Overlay logic for Add Event
        val addEventOverlay = binding.root.findViewById<android.widget.FrameLayout>(R.id.addEventOverlay)
        val inflater = LayoutInflater.from(requireContext())

        binding.btnNewEvent.setOnClickListener {
            // Always remove previous view to reset form
            addEventOverlay.removeAllViews()
            val addEventView = inflater.inflate(R.layout.fragment_add_event, addEventOverlay, false)
            addEventOverlay.addView(addEventView)

            // Get references to EditTexts inside TextInputLayouts
            val tilTitle = addEventView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilTitle)
            val tilDescription = addEventView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilDescription)
            val tilLocation = addEventView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLocation)
            val etTitle = tilTitle.editText
            val etDescription = tilDescription.editText
            val etLocation = tilLocation.editText
            etTitle?.setText("")
            etDescription?.setText("")
            etLocation?.setText("")

            // Date and time pickers
            val btnDatePicker = addEventView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDatePicker)
            val btnTimePicker = addEventView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnTimePicker)
            // NEW: end date/time buttons
            val btnEndDatePicker = addEventView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEndDatePicker)
            val btnEndTimePicker = addEventView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEndTimePicker)

            val startCalendar = Calendar.getInstance()
            val endCalendar = Calendar.getInstance()

            // Flags to track whether user explicitly selected end date/time
            var endDateSelected = false
            var endTimeSelected = false

            // Set initial button text
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            btnDatePicker.text = dateFormat.format(startCalendar.time)
            btnTimePicker.text = timeFormat.format(startCalendar.time)
            btnEndDatePicker.text = dateFormat.format(endCalendar.time)
            btnEndTimePicker.text = timeFormat.format(endCalendar.time)

            btnDatePicker.setOnClickListener {
                val context = requireContext()
                val datePicker = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        startCalendar.set(Calendar.YEAR, year)
                        startCalendar.set(Calendar.MONTH, month)
                        startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        btnDatePicker.text = dateFormat.format(startCalendar.time)
                        // ensure end is not before start: if so, move end to same day
                        if (endCalendar.time.before(startCalendar.time)) {
                            endCalendar.time = startCalendar.time
                            btnEndDatePicker.text = dateFormat.format(endCalendar.time)
                        }
                    },
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)
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
                        startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        startCalendar.set(Calendar.MINUTE, minute)
                        startCalendar.set(Calendar.SECOND, 0)
                        btnTimePicker.text = timeFormat.format(startCalendar.time)
                        // keep end >= start
                        if (endCalendar.time.before(startCalendar.time)) {
                            endCalendar.time = startCalendar.time
                            btnEndDatePicker.text = dateFormat.format(endCalendar.time)
                            btnEndTimePicker.text = timeFormat.format(endCalendar.time)
                        }
                    },
                    startCalendar.get(Calendar.HOUR_OF_DAY),
                    startCalendar.get(Calendar.MINUTE),
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

            // NEW: End date/time pickers logic
            btnEndDatePicker.setOnClickListener {
                val context = requireContext()
                val datePicker = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        endCalendar.set(Calendar.YEAR, year)
                        endCalendar.set(Calendar.MONTH, month)
                        endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        btnEndDatePicker.text = dateFormat.format(endCalendar.time)
                        endDateSelected = true
                        // If end is before start, adjust end to start
                        if (endCalendar.time.before(startCalendar.time)) {
                            endCalendar.time = startCalendar.time
                            btnEndDatePicker.text = dateFormat.format(endCalendar.time)
                        }
                    },
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.setOnShowListener {
                    val dp = datePicker
                    dp.window?.setBackgroundDrawableResource(android.R.color.white)
                    val decorView = dp.window?.decorView
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
                datePicker.show()
            }

            btnEndTimePicker.setOnClickListener {
                val context = requireContext()
                val timePicker = android.app.TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        endCalendar.set(Calendar.MINUTE, minute)
                        endCalendar.set(Calendar.SECOND, 0)
                        btnEndTimePicker.text = timeFormat.format(endCalendar.time)
                        endTimeSelected = true
                        if (endCalendar.time.before(startCalendar.time)) {
                            // If end is before start, move end to one hour after start
                            endCalendar.time = java.util.Date(startCalendar.time.time + 3600_000)
                            btnEndDatePicker.text = dateFormat.format(endCalendar.time)
                            btnEndTimePicker.text = timeFormat.format(endCalendar.time)
                        }
                    },
                    endCalendar.get(Calendar.HOUR_OF_DAY),
                    endCalendar.get(Calendar.MINUTE),
                    false
                )
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

            val btnCancel = addEventView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)
            val btnSave = addEventView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveEvent)
            btnCancel.setOnClickListener {
                addEventOverlay.visibility = View.GONE
            }
            btnSave.setOnClickListener {
                val title = etTitle?.text?.toString()?.trim() ?: ""
                val description = etDescription?.text?.toString()?.trim()
                val location = etLocation?.text?.toString()?.trim()
                val startTime = startCalendar.time
                // Validate end time was explicitly selected; require user to pick end date and time
                if (!endDateSelected || !endTimeSelected) {
                    // Show simple inline validation by updating end buttons text color briefly
                    btnEndDatePicker.setTextColor(android.graphics.Color.RED)
                    btnEndTimePicker.setTextColor(android.graphics.Color.RED)
                    // reset color after short delay
                    btnEndDatePicker.postDelayed({
                        btnEndDatePicker.setTextColor(android.graphics.Color.BLACK)
                        btnEndTimePicker.setTextColor(android.graphics.Color.BLACK)
                    }, 1200)
                    return@setOnClickListener
                }
                val endTime = endCalendar.time
                if (endTime.before(startTime)) {
                    // ensure end is after start
                    btnEndDatePicker.setTextColor(android.graphics.Color.RED)
                    btnEndTimePicker.setTextColor(android.graphics.Color.RED)
                    btnEndDatePicker.postDelayed({
                        btnEndDatePicker.setTextColor(android.graphics.Color.BLACK)
                        btnEndTimePicker.setTextColor(android.graphics.Color.BLACK)
                    }, 1200)
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    eventsViewModel.addEvent(title, description, startTime, endTime, location)
                }

                addEventOverlay.visibility = View.GONE
            }
            addEventOverlay.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter(onDayEventClick = { event ->
            val bundle = Bundle().apply {
                putString("eventId", event.id)
                putString("title", event.title)
                putString("description", event.description)
                putString("location", event.location)
                putLong("date", event.startTime.time)
                putLong("time", event.startTime.time)
                putLong("endTime", event.endTime?.time ?: 0L)
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

//    override fun onResume() {
//        super.onResume()
//        // Reload events every time the fragment becomes visible
//        eventsViewModel.loadEvents("sampleUserId")
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.varsitycollege.schedulist.ui.main.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.databinding.FragmentEventViewBinding
import EventsViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EventViewFragment : Fragment() {
    private var _binding: FragmentEventViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var eventsViewModel: EventsViewModel
    private var eventId: String = ""
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()
    private var selectedEndDate: Calendar = Calendar.getInstance()
    private var selectedEndTime: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared ViewModel
        val repository = EventsRepository(requireContext())
        val factory = EventsViewModelFactory(repository)
        eventsViewModel = ViewModelProvider(requireActivity(), factory).get(EventsViewModel::class.java)

        val args = arguments
        eventId = args?.getString("eventId") ?: ""
        val title = args?.getString("title") ?: ""
        val description = args?.getString("description") ?: ""
        val location = args?.getString("location") ?: ""
        val attachment = args?.getString("attachment") ?: ""
        val date = args?.getLong("date") ?: 0L
        val time = args?.getLong("time") ?: 0L
        val endTimeMillis = args?.getLong("endTime") ?: 0L

        val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

        binding.etEventTitle.setText(title)
        binding.etEventDescription.setText(description)
        binding.etLocationName.setText(location)
        binding.etAttachmentName.setText(attachment)

        if (date != 0L) {
            selectedDate.timeInMillis = date
            binding.chipDate.text = dateFormatter.format(Date(date))
        } else {
            binding.chipDate.text = ""
        }
        if (time != 0L) {
            selectedTime.timeInMillis = time
            binding.chipTime.text = timeFormatter.format(Date(time))
        } else {
            binding.chipTime.text = ""
        }

        // Display end date and end time in separate chips
        if (endTimeMillis != 0L) {
            selectedEndTime.timeInMillis = endTimeMillis
            selectedEndDate.timeInMillis = endTimeMillis
            binding.chipEndDate.text = dateFormatter.format(Date(endTimeMillis))
            binding.chipEndTime.text = timeFormatter.format(Date(endTimeMillis))
        } else {
            binding.chipEndDate.text = ""
            binding.chipEndTime.text = ""
        }

        setEditMode(false)

        binding.btnDelete.setOnClickListener {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        val success = eventsViewModel.deleteEvent(eventId)
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                "Event deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Navigate back
                            requireActivity().supportFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete event",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnEdit.setOnClickListener {
            val isEditing = binding.etEventTitle.isEnabled
            if (isEditing) {
                // Save mode: disable editing and update button text
                setEditMode(false)
                binding.btnEdit.text = getString(R.string.event_view_button_edit)

                // Save the updated event
                val updatedTitle = binding.etEventTitle.text.toString()
                val updatedDescription = binding.etEventDescription.text.toString()
                val updatedLocation = binding.etLocationName.text.toString()

                // Combine date and time for start
                val startCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                    set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Combine date and time for end
                val endCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedEndDate.get(Calendar.YEAR))
                    set(Calendar.MONTH, selectedEndDate.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, selectedEndDate.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, selectedEndTime.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, selectedEndTime.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val updatedStartTime = startCalendar.time
                val updatedEndTime = endCalendar.time

                // Validate: end time must be after start time
                if (updatedEndTime.before(updatedStartTime) || updatedEndTime == updatedStartTime) {
                    Toast.makeText(
                        requireContext(),
                        "End time must be after start time",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Create updated event object
                lifecycleScope.launch {
                    val currentEvent = eventsViewModel.getEventById(eventId)
                    if (currentEvent != null) {
                        val updatedEvent = currentEvent.copy(
                            title = updatedTitle,
                            description = updatedDescription,
                            location = updatedLocation,
                            startTime = updatedStartTime,
                            endTime = updatedEndTime
                        )
                        eventsViewModel.updateEvent(updatedEvent)
                    }
                }
            } else {
                // Edit mode: enable editing and update button text
                setEditMode(true)
                binding.btnEdit.text = getString(R.string.save)
            }
        }

        binding.chipDate.setOnClickListener {
            if (binding.etEventTitle.isEnabled) {
                val year = selectedDate.get(Calendar.YEAR)
                val month = selectedDate.get(Calendar.MONTH)
                val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(requireContext(), { _, y, m, d ->
                    selectedDate.set(Calendar.YEAR, y)
                    selectedDate.set(Calendar.MONTH, m)
                    selectedDate.set(Calendar.DAY_OF_MONTH, d)
                    binding.chipDate.text = dateFormatter.format(selectedDate.time)
                }, year, month, day).show()
            }
        }
        binding.chipTime.setOnClickListener {
            if (binding.etEventTitle.isEnabled) {
                val hour = selectedTime.get(Calendar.HOUR_OF_DAY)
                val minute = selectedTime.get(Calendar.MINUTE)
                TimePickerDialog(requireContext(), { _, h, m ->
                    selectedTime.set(Calendar.HOUR_OF_DAY, h)
                    selectedTime.set(Calendar.MINUTE, m)
                    binding.chipTime.text = timeFormatter.format(selectedTime.time)
                }, hour, minute, false).show()
            }
        }

        binding.chipEndDate.setOnClickListener {
            if (binding.etEventTitle.isEnabled) {
                val year = selectedEndDate.get(Calendar.YEAR)
                val month = selectedEndDate.get(Calendar.MONTH)
                val day = selectedEndDate.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(requireContext(), { _, y, m, d ->
                    selectedEndDate.set(Calendar.YEAR, y)
                    selectedEndDate.set(Calendar.MONTH, m)
                    selectedEndDate.set(Calendar.DAY_OF_MONTH, d)
                    binding.chipEndDate.text = dateFormatter.format(selectedEndDate.time)
                }, year, month, day).show()
            }
        }

        binding.chipEndTime.setOnClickListener {
            if (binding.etEventTitle.isEnabled) {
                val hour = selectedEndTime.get(Calendar.HOUR_OF_DAY)
                val minute = selectedEndTime.get(Calendar.MINUTE)
                TimePickerDialog(requireContext(), { _, h, m ->
                    selectedEndTime.set(Calendar.HOUR_OF_DAY, h)
                    selectedEndTime.set(Calendar.MINUTE, m)
                    binding.chipEndTime.text = timeFormatter.format(selectedEndTime.time)
                }, hour, minute, false).show()
            }
        }
    }

    private fun setEditMode(enabled: Boolean) {
        binding.etEventTitle.isEnabled = enabled
        binding.etEventTitle.isFocusable = enabled
        binding.etEventTitle.isFocusableInTouchMode = enabled
        binding.etEventDescription.isEnabled = enabled
        binding.etEventDescription.isFocusable = enabled
        binding.etEventDescription.isFocusableInTouchMode = enabled
        binding.etLocationName.isEnabled = enabled
        binding.etLocationName.isFocusable = enabled
        binding.etLocationName.isFocusableInTouchMode = enabled
        binding.etAttachmentName.isEnabled = enabled
        binding.etAttachmentName.isFocusable = enabled
        binding.etAttachmentName.isFocusableInTouchMode = enabled
        binding.chipDate.isClickable = enabled
        binding.chipTime.isClickable = enabled
        binding.chipEndDate.isClickable = enabled
        binding.chipEndTime.isClickable = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

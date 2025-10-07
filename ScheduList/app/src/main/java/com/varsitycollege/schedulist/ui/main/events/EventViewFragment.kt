package com.varsitycollege.schedulist.ui.main.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.databinding.FragmentEventViewBinding
import java.text.SimpleDateFormat
import java.util.*

class EventViewFragment : Fragment() {
    private var _binding: FragmentEventViewBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments
        val title = args?.getString("title") ?: ""
        val description = args?.getString("description") ?: ""
        val location = args?.getString("location") ?: ""
        val attachment = args?.getString("attachment") ?: ""
        val date = args?.getLong("date") ?: 0L
        val time = args?.getLong("time") ?: 0L

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

        setEditMode(false)

        binding.btnEdit.setOnClickListener {
            val isEditing = binding.etEventTitle.isEnabled
            if (isEditing) {
                // Save mode: disable editing and update button text
                setEditMode(false)
                binding.btnEdit.text = getString(R.string.event_view_button_edit)
                // Optionally, handle saving updated data here
                // val updatedTitle = binding.etEventTitle.text.toString()
                // val updatedDescription = binding.etEventDescription.text.toString()
                // val updatedLocation = binding.etLocationName.text.toString()
                // val updatedAttachment = binding.etAttachmentName.text.toString()
                // val updatedDate = selectedDate.timeInMillis
                // val updatedTime = selectedTime.timeInMillis
                // Save to ViewModel, database, etc.
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

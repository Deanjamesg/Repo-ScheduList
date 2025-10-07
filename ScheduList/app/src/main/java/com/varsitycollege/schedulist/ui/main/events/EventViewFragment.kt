package com.varsitycollege.schedulist.ui.main.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.varsitycollege.schedulist.databinding.FragmentEventViewBinding
import java.text.SimpleDateFormat
import java.util.*

class EventViewFragment : Fragment() {
    private var _binding: FragmentEventViewBinding? = null
    private val binding get() = _binding!!

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
        val date = args?.getLong("date") ?: 0L
        val time = args?.getLong("time") ?: 0L

        val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

        binding.tvEventTitle.text = title
        binding.tvEventDescription.text = description
        binding.tvLocationName.text = location
        binding.chipDate.text = if (date != 0L) dateFormatter.format(Date(date)) else ""
        binding.chipTime.text = if (time != 0L) timeFormatter.format(Date(time)) else ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

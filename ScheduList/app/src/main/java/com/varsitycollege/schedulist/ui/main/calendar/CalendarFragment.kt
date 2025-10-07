package com.varsitycollege.schedulist.ui.main.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.data.repository.CalendarRepository
import com.varsitycollege.schedulist.databinding.FragmentCalendarBinding
import com.varsitycollege.schedulist.ui.adapter.MonthGridAdapter
import com.varsitycollege.schedulist.util.GridSpacingItemDecoration

// This is our Fragment for the Calendar screen. It sets up the view and
// observes the ViewModel for the list of days to display in our grid.

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var monthAdapter: MonthGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = CalendarRepository()
        val factory = CalendarViewModelFactory(repository)
        calendarViewModel = ViewModelProvider(this, factory).get(CalendarViewModel::class.java)

        setupRecyclerView()

        // We start observing the 'monthList' from our ViewModel.
        calendarViewModel.monthList.observe(viewLifecycleOwner) { monthDayList ->
            // When we get the new list, we give it to our adapter to display.
            monthAdapter.submitList(monthDayList)
        }

        // Now we tell the ViewModel to start loading the data.
        calendarViewModel.loadCalendarData("sampleUserId")
    }

    private fun setupRecyclerView() {
        monthAdapter = MonthGridAdapter()
        binding.calendarRecyclerView.apply {
            // We set the layout manager to a grid with 7 columns for our calendar.
            layoutManager = GridLayoutManager(context, 7)
            adapter = monthAdapter
            // This adds a small gap between all the calendar cells.
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            addItemDecoration(GridSpacingItemDecoration(7, spacingInPixels, true))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
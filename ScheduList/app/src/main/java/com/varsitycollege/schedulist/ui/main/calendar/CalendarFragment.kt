package com.varsitycollege.schedulist.ui.main.calendar

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.data.repository.CalendarRepository
import com.varsitycollege.schedulist.databinding.FragmentCalendarBinding
import com.varsitycollege.schedulist.ui.adapter.MonthDay
import com.varsitycollege.schedulist.ui.adapter.MonthGridAdapter
import com.varsitycollege.schedulist.ui.adapter.SimpleListAdapter
import com.varsitycollege.schedulist.ui.adapter.SimpleListItem
import com.varsitycollege.schedulist.util.GridSpacingItemDecoration

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

        // Updating the grid when data changes
        calendarViewModel.monthList.observe(viewLifecycleOwner) { monthDayList ->
            monthAdapter.submitList(monthDayList)
        }

        // Updating the Month Name (e.g. November 2025)
        calendarViewModel.currentMonthText.observe(viewLifecycleOwner) { text ->
            binding.tvMonthName.text = text
        }

        // Navigation buttons
        binding.btnNextMonth.setOnClickListener {
            calendarViewModel.nextMonth()
        }

        binding.btnPrevMonth.setOnClickListener {
            calendarViewModel.previousMonth()
        }

        calendarViewModel.loadCalendarData("sampleUserId")
    }

    private fun setupRecyclerView() {
        // Passing the click function to the adapter
        monthAdapter = MonthGridAdapter { monthDay ->
            showDayDetailsPopup(monthDay)
        }

        binding.calendarRecyclerView.apply {
            // Using 3 columns to make the cards wider and readable
            layoutManager = GridLayoutManager(context, 3)
            adapter = monthAdapter

            // Adding spacing between the cards
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            addItemDecoration(GridSpacingItemDecoration(3, spacingInPixels, true))
        }
    }

    // Logic to show the popup window with the specific day's tasks
    private fun showDayDetailsPopup(day: MonthDay) {
        if (day.dayOfMonth.isEmpty()) return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_day_details, null)

        val tvDateTitle = dialogView.findViewById<TextView>(R.id.tvDateTitle)
        val rvDayTasks = dialogView.findViewById<RecyclerView>(R.id.rvDayTasks)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btnClose)

        tvDateTitle.text = "${binding.tvMonthName.text} ${day.dayOfMonth}"

        // We reuse the SimpleListAdapter here because it looks good for a list
        val dayListAdapter = SimpleListAdapter { _, _ -> }

        rvDayTasks.layoutManager = LinearLayoutManager(requireContext())
        rvDayTasks.adapter = dayListAdapter

        // Convert the data so the SimpleListAdapter can understand it
        val simpleListItems = day.tasks.map { task ->
            SimpleListItem(
                id = task.id ?: "",
                title = task.title,
                date = task.dueDate,
                isCompleted = task.isCompleted
            )
        }
        dayListAdapter.submitList(simpleListItems)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
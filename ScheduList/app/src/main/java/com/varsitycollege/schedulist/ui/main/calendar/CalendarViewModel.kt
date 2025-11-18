package com.varsitycollege.schedulist.ui.main.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.repository.CalendarRepository
import com.varsitycollege.schedulist.ui.adapter.MonthDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// This is the ViewModel for our Calendar screen. It gets the raw tasks
// from the repository and then does the logic to build our list of days for the grid.

class CalendarViewModel(private val repository: CalendarRepository) : ViewModel() {

    // Holds the current month we are viewing (defaults to today)
    private val currentCalendar = Calendar.getInstance()

    // LiveData for the Header Text (e.g., "October 2025")
    private val _currentMonthText = MutableLiveData<String>()
    val currentMonthText: LiveData<String> = _currentMonthText

    // Store raw tasks to re-filter when month changes
    private var rawTasksList: List<Task> = emptyList()

    // This is the final list of Day objects for our MonthGridAdapter.
    private val _monthList = MutableLiveData<List<MonthDay>>()
    val monthList: LiveData<List<MonthDay>> = _monthList

    fun loadCalendarData(userId: String) {
        // Initial load of the month title
        updateMonthText()

        // We get the raw tasks from the repository.
        repository.getCalendarTasks(userId).observeForever { tasks ->
            // Save the tasks so we can reuse them when changing months
            rawTasksList = tasks
            // Once we have the tasks, we format them for the grid view.
            generateMonthGrid()
        }
    }

    // Go to the next month
    fun nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1)
        updateMonthText()
        generateMonthGrid()
    }

    // Go to the previous month
    fun previousMonth() {
        currentCalendar.add(Calendar.MONTH, -1)
        updateMonthText()
        generateMonthGrid()
    }

    private fun updateMonthText() {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        _currentMonthText.value = formatter.format(currentCalendar.time)
    }

    // This function prepares the data for the calendar grid.
    private fun generateMonthGrid() {
        // We use the saved list of tasks
        val tasks = rawTasksList
        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthDays = mutableListOf<MonthDay>()

        // A simple loop to create cells for each day of the month.
        for (i in 1..daysInMonth) {
            val tasksForDay = tasks.filter {
                val taskCalendar = Calendar.getInstance().apply { time = it.dueDate }
                // Check if Year, Month, and Day match the current view
                taskCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        taskCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                        taskCalendar.get(Calendar.DAY_OF_MONTH) == i
            }
            monthDays.add(MonthDay(dayOfMonth = i.toString(), tasks = tasksForDay))
        }
        _monthList.value = monthDays
    }
}
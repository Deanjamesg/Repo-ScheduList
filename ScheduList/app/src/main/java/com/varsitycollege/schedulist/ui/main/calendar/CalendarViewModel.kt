package com.varsitycollege.schedulist.ui.main.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.repository.CalendarRepository
import com.varsitycollege.schedulist.ui.adapter.MonthDay
import java.util.Calendar

// This is the ViewModel for our Calendar screen. It gets the raw tasks
// from the repository and then does the logic to build our list of days for the grid.

class CalendarViewModel(private val repository: CalendarRepository) : ViewModel() {

    // This is the final list of Day objects for our MonthGridAdapter.
    private val _monthList = MutableLiveData<List<MonthDay>>()
    val monthList: LiveData<List<MonthDay>> = _monthList

    fun loadCalendarData(userId: String) {
        // We get the raw tasks from the repository.
        repository.getCalendarTasks(userId).observeForever { tasks ->
            // Once we have the tasks, we format them for the grid view.
            formatListForMonthView(tasks)
        }
    }

    // This function prepares the data for the calendar grid.
    private fun formatListForMonthView(tasks: List<Task>) {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthDays = mutableListOf<MonthDay>()

        // A simple loop to create cells for each day of the month.
        for (i in 1..daysInMonth) {
            val tasksForDay = tasks.filter {
                val taskCalendar = Calendar.getInstance().apply { time = it.dueDate }
                taskCalendar.get(Calendar.DAY_OF_MONTH) == i
            }
            monthDays.add(MonthDay(dayOfMonth = i.toString(), tasks = tasksForDay))
        }
        _monthList.value = monthDays
    }
}
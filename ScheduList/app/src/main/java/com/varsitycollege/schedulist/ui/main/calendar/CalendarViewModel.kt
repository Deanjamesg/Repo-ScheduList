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
import android.util.Log

class CalendarViewModel(private val repository: CalendarRepository) : ViewModel() {

    private val currentCalendar = Calendar.getInstance()

    private val TAG = "CALENDAR_VM" // For logging purposes

    private val _currentMonthText = MutableLiveData<String>()
    val currentMonthText: LiveData<String> = _currentMonthText

    // We store the raw tasks here so we can use them when generating the grid
    private var rawTasksList: List<Task> = emptyList()

    private val _monthList = MutableLiveData<List<MonthDay>>()
    val monthList: LiveData<List<MonthDay>> = _monthList

    fun loadCalendarData(userId: String) {
        Log.d(TAG, "Request received. Starting data fetch for user $userId") // Log point 1
        updateMonthText()
        repository.getCalendarTasks(userId).observeForever { tasks ->
            Log.d(TAG, "Repository returned ${tasks.size} raw task items.") // Log point 2
            rawTasksList = tasks
            generateMonthGrid()
        }
    }

    fun nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1)
        updateMonthText()
        generateMonthGrid()
    }

    fun previousMonth() {
        currentCalendar.add(Calendar.MONTH, -1)
        updateMonthText()
        generateMonthGrid()
    }

    private fun updateMonthText() {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        _currentMonthText.value = formatter.format(currentCalendar.time)
    }

    private fun generateMonthGrid() {
        val tasks = rawTasksList
        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthDays = mutableListOf<MonthDay>()

        // Start directly from day 1 (List View style)
        for (i in 1..daysInMonth) {
            val tasksForDay = tasks.filter {
                val taskCalendar = Calendar.getInstance().apply { time = it.dueDate }
                taskCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        taskCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                        taskCalendar.get(Calendar.DAY_OF_MONTH) == i
            }
            monthDays.add(MonthDay(dayOfMonth = i.toString(), tasks = tasksForDay))
        }
        _monthList.value = monthDays
        Log.d(TAG, "Grid generated with ${monthDays.size} cells. Ready for adapter.") // Log point 3
    }
}
package com.varsitycollege.schedulist.ui.main.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.ui.adapter.MonthDay
import com.varsitycollege.schedulist.ui.adapter.TaskListItem
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {

    private val _displayList = MutableLiveData<List<TaskListItem>>()
    val displayList: LiveData<List<TaskListItem>> = _displayList

    private val _monthList = MutableLiveData<List<MonthDay>>()
    val monthList: LiveData<List<MonthDay>> = _monthList

    // Keeping a local copy to help with switching views
    private val currentTasks = mutableListOf<Task>()

    // Added userId here so the fragment can pass it in
    fun startListeningForTasks(userId: String) {
        viewModelScope.launch {
            repository.getTasks().observeForever { tasks ->
                currentTasks.clear()
                currentTasks.addAll(tasks)
                // Updating all views when data changes
                formatListForDayView(tasks)
                formatListForWeekView(tasks)
                formatListForMonthView(tasks)
            }
        }
    }

    // Added this function so we can save from the fragment
    fun addTask(title: String, description: String?, dueDate: Date) {
        viewModelScope.launch {
            val newTask = repository.addTask(title, description, dueDate)
            if (newTask != null) {
                currentTasks.add(newTask)
                formatListForDayView(currentTasks)
            }
        }
    }

    fun setViewType(viewType: String) {
        val tasks = currentTasks
        when (viewType) {
            "Day" -> formatListForDayView(tasks)
            "Week" -> formatListForWeekView(tasks)
        }
    }

    private fun formatListForDayView(tasks: List<Task>) {
        _displayList.value = tasks
            .sortedBy { it.isCompleted }
            .map { task -> TaskListItem.DayTaskItem(task) }
    }

    private fun formatListForWeekView(tasks: List<Task>) {
        val formattedList = mutableListOf<TaskListItem>()
        // Grouping tasks by date for the week view
        tasks.sortedBy { it.dueDate }
            .groupBy {
                val cal = Calendar.getInstance()
                cal.time = it.dueDate
                "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
            }
            .forEach { (dateString, tasksOnThatDate) ->
                formattedList.add(TaskListItem.HeaderItem(dateString))
                tasksOnThatDate.forEach { task ->
                    formattedList.add(TaskListItem.WeekTaskItem(task))
                }
            }
        _displayList.value = formattedList
    }

    private fun formatListForMonthView(tasks: List<Task>) {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthDays = mutableListOf<MonthDay>()

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
package com.varsitycollege.schedulist.ui.main.tasks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.model.TaskList
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.ui.adapter.MonthDay
import com.varsitycollege.schedulist.ui.adapter.TaskListItem
import java.util.Calendar
import java.util.Date

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {

    private val TAG = "TasksViewModel"

    // Source data from Firebase
    private val allTasksLiveData = repository.getTasksLiveData()

    // LiveData for tasks with due dates
    private val _displayList = MediatorLiveData<List<TaskListItem>>()
    val displayList: LiveData<List<TaskListItem>> = _displayList

    // LiveData for month view
    private val _monthList = MediatorLiveData<List<MonthDay>>()
    val monthList: LiveData<List<MonthDay>> = _monthList

    // LiveData for task lists
    private val _taskLists = repository.getTaskListsLiveData()
    val taskLists: LiveData<List<TaskList>> = _taskLists

    private var currentViewType = "Day"
    private var selectedTaskListId: String? = null // null = show all

    init {
        Log.d(TAG, "TasksViewModel initialized")

        // Set up mediator to react to data changes
        _displayList.addSource(allTasksLiveData) { tasks ->
            Log.d(TAG, "Tasks updated in ViewModel: ${tasks.size} tasks")
            updateDisplayList(tasks)
        }

        _monthList.addSource(allTasksLiveData) { tasks ->
            updateMonthList(tasks)
        }

        // Log when task lists change
        _taskLists.observeForever { lists ->
            Log.d(TAG, "Task lists updated in ViewModel: ${lists.size} lists")
        }
    }

    // Add a new task
    fun addTask(
        title: String,
        description: String?,
        dueDate: Date,
        taskListId: String
    ) {
        viewModelScope.launch {
            repository.addTask(title, description, dueDate, taskListId)
            // LiveData observer updates automatically
        }
    }


    fun toggleTaskCompletion(taskId: String) {
        Log.d(TAG, "toggleTaskCompletion called for taskId: $taskId")

        viewModelScope.launch {
            try {
                val success = repository.toggleTaskCompletion(taskId)
                Log.d(TAG, "Toggle result: ${if (success) "SUCCESS" else "FAILED"}")

                if (!success) {
                    Log.e(TAG, "Failed to toggle task completion")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling task completion", e)
            }
        }
    }

    fun addTaskList(name: String) {
        viewModelScope.launch {
            repository.addTaskList(name)
        }
    }

    fun setViewType(viewType: String) {
        Log.d(TAG, "Setting view type to: $viewType")
        currentViewType = viewType

        // Re-trigger formatting
        val tasks = allTasksLiveData.value ?: return
        updateDisplayList(tasks)
        updateMonthList(tasks)
    }

    fun filterByTaskList(taskListId: String?) {
        Log.d(TAG, "Filtering by task list: ${taskListId ?: "All Tasks"}")
        selectedTaskListId = taskListId

        // Re-trigger formatting
        val tasks = allTasksLiveData.value ?: return
        updateDisplayList(tasks)
        updateMonthList(tasks)
    }

    private fun updateDisplayList(allTasks: List<Task>) {
        Log.d(TAG, "updateDisplayList: ${allTasks.size} tasks, viewType=$currentViewType")

        // Apply task list filter
        val filteredTasks = if (selectedTaskListId != null) {
            allTasks.filter { it.taskListId == selectedTaskListId }
        } else {
            allTasks
        }

        Log.d(TAG, "After list filter: ${filteredTasks.size} tasks")

        // Format based on view type
        when (currentViewType) {
            "Day" -> {
                // Day view can show all tasks
                formatListForDayView(filteredTasks)
            }
            "Week", "Month" -> {
                // Week and Month views need due dates
                val tasksWithDates = filteredTasks.filter { it.dueDate != null }
                Log.d(TAG, "Tasks with due dates: ${tasksWithDates.size}")

                when (currentViewType) {
                    "Week" -> formatListForWeekView(tasksWithDates)
                    "Month" -> formatListForMonthView(tasksWithDates)
                }
            }
        }
    }

    private fun updateMonthList(allTasks: List<Task>) {
        // Only process if in month view
        if (currentViewType != "Month") return

        // Apply task list filter
        val filteredTasks = if (selectedTaskListId != null) {
            allTasks.filter { it.taskListId == selectedTaskListId }
        } else {
            allTasks
        }

        // Only include tasks with due dates
        val tasksWithDates = filteredTasks.filter { it.dueDate != null }

        formatListForMonthView(tasksWithDates)
    }

    private fun formatListForDayView(tasks: List<Task>) {
        _displayList.value = tasks
            .sortedBy { it.isCompleted }
            .map { task -> TaskListItem.DayTaskItem(task) }
    }

    private fun formatListForWeekView(tasks: List<Task>) {
        // Only include tasks with due dates
        val tasksWithDates = tasks.filter { it.dueDate != null }

        if (tasksWithDates.isEmpty()) {
            Log.d(TAG, "No tasks with due dates for week view")
            _displayList.value = emptyList()
            return
        }

        val grouped = tasksWithDates
            .sortedBy { it.dueDate }
            .groupBy { task ->
                val calendar = Calendar.getInstance().apply { time = task.dueDate!! }
                "${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault())}"
            }

        val itemList = mutableListOf<TaskListItem>()
        grouped.forEach { (date, tasksForDay) ->
            // Add header
            itemList.add(TaskListItem.HeaderItem(date))
            // Add tasks
            tasksForDay.forEach { task ->
                itemList.add(TaskListItem.WeekTaskItem(task))
            }
        }
        _displayList.value = itemList
    }

    private fun formatListForMonthView(tasks: List<Task>) {
        // Only include tasks with due dates
        val tasksWithDates = tasks.filter { it.dueDate != null }

        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val monthDays = mutableListOf<MonthDay>()

        for (i in 1..daysInMonth) {
            val tasksForDay = tasksWithDates.filter { task ->
                val taskCalendar = Calendar.getInstance().apply { time = task.dueDate!! }
                taskCalendar.get(Calendar.DAY_OF_MONTH) == i &&
                        taskCalendar.get(Calendar.MONTH) == currentMonth &&
                        taskCalendar.get(Calendar.YEAR) == currentYear
            }
            monthDays.add(MonthDay(dayOfMonth = i.toString(), tasks = tasksForDay))
        }
        _monthList.value = monthDays
    }

}
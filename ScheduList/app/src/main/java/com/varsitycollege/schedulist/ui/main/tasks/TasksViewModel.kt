package com.varsitycollege.schedulist.ui.main.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.ui.adapter.MonthDay
import com.varsitycollege.schedulist.ui.adapter.TaskListItem
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

// This is the ViewModel for the tasks screen. It gets the raw data from the
// repository and then transforms it into the specific list that our adapter needs
// (like adding date headers for the week view).

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {

    private lateinit var rawTasks: LiveData<List<Task>>

    // This is the final formatted list for our Day/Week adapter.
    private val _displayList = MutableLiveData<List<TaskListItem>>()
    val displayList: LiveData<List<TaskListItem>> = _displayList

    // This is the new list specifically for our MonthGridAdapter.
    private val _monthList = MutableLiveData<List<MonthDay>>()
    val monthList: LiveData<List<MonthDay>> = _monthList

    private val currentTasks = mutableListOf<Task>()

    fun loadTasks() {
        viewModelScope.launch {
            val tasks = repository.getTasks()
            currentTasks.clear()
            currentTasks.addAll(tasks as Collection<Task>)
            formatListForDayView(currentTasks)
        }
    }

//    fun addTask(
//        title: String,
//        description: String?,
//        dueDate: Date,
//
//    ) {
//        viewModelScope.launch {
//            // We call the repository to add the task via the API.
////            val newTask = repository.addTask(title, description, dueDate)
//            // title, description, dueDate
//            if (newTask != null) {
//                // If it's successful, we add the new task to our local list and refresh the UI.
//                currentTasks.add(newTask)
//                formatListForDayView(currentTasks)
//            }
//        }
//    }

    suspend fun startListeningForTasks() {
        rawTasks = repository.getTasks()
        rawTasks.observeForever { tasks ->
            formatListForDayView(tasks)
            formatListForWeekView(tasks)
            formatListForMonthView(tasks)
        }
    }

    fun setViewType(viewType: String) {
        val tasks = rawTasks.value ?: return
        when (viewType) {
            "Day" -> formatListForDayView(tasks)
            "Week" -> formatListForWeekView(tasks)
        }
    }

    private fun formatListForDayView(tasks: List<Task>) {
        // This just maps our raw tasks to the DayTaskItem for the adapter.
        _displayList.value = tasks
            .sortedBy { it.isCompleted }
            .map { task -> TaskListItem.DayTaskItem(task) }
    }

    private fun formatListForWeekView(tasks: List<Task>) {
        // This logic would group tasks by date and add headers.
    }

    // This is the new function to prepare data for the calendar grid.
    private fun formatListForMonthView(tasks: List<Task>) {
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthDays = mutableListOf<MonthDay>()

        // A simple loop to create 31 days for our grid.
        // A real implementation would be more complex to handle empty start days.
        for (i in 1..daysInMonth) {
            // Find all the tasks that happen on this specific day.
            val tasksForDay = tasks.filter {
                val taskCalendar = Calendar.getInstance().apply { time = it.dueDate }
                taskCalendar.get(Calendar.DAY_OF_MONTH) == i
            }
            // Add a new MonthDay object to our list.
            monthDays.add(MonthDay(dayOfMonth = i.toString(), tasks = tasksForDay))
        }
        _monthList.value = monthDays
    }


    // This gets called from our "Add New Task" fragment.
//    fun addTask(newTask: Task) {
//        // Just add the new task object to the "tasks" collection in Firestore.
//        // The snapshot listener will automatically pick up the change and update the UI.
//        tasksCollection.add(newTask)
//    }

    // Call this to update the spinner's list
//    fun updateTaskListNames(newList: List<String>) {
//        _taskListNames.value = newList
//    }

//    // Update an existing task in Firestore
//    fun updateTask(taskId: String, updatedTask: Task) {
//        tasksCollection.document(taskId).set(updatedTask)
//    }
//
//    // Delete a task from Firestore
//    fun deleteTask(taskId: String) {
//        tasksCollection.document(taskId).delete()
//    }

    // Note: For week view grouping, you should extract the date part from dueDate (e.g., using OffsetDateTime.toLocalDate())
}
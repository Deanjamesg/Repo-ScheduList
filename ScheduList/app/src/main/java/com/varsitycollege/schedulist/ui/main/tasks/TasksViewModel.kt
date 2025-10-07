package com.varsitycollege.schedulist.ui.main.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.ui.adapter.TaskListItem

// This is the ViewModel for the tasks screen. It gets the raw data from the
// repository and then transforms it into the specific list that our adapter needs
// (like adding date headers for the week view).

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {

    // This holds the raw list of tasks from the repository.
    private lateinit var rawTasks: LiveData<List<Task>>

    // This is the final formatted list for our TasksAdapter (Day/Week view).
    private val _displayList = MutableLiveData<List<TaskListItem>>()
    val displayList: LiveData<List<TaskListItem>> = _displayList

    // We call this from the Fragment to start the data loading process.
    fun startListeningForTasks(userId: String) {
        rawTasks = repository.getTasks(userId)
        rawTasks.observeForever { tasks ->
            // Whenever our raw data changes, we re-format it for the Day view by default.
            formatListForDayView(tasks)
        }
    }

    // This function is called by the Fragment when the spinner changes.
    fun setViewType(viewType: String) {
        val tasks = rawTasks.value ?: return // Get the current list of tasks
        when (viewType) {
            "Day" -> formatListForDayView(tasks)
            "Week" -> formatListForWeekView(tasks)
            // "Month" view will be handled by a different adapter in the fragment.
        }
    }

    // Formats the list for the detailed Day view.
    private fun formatListForDayView(tasks: List<Task>) {
        _displayList.value = tasks.map { task ->
            TaskListItem.DayTaskItem(task)
        }
    }

    // Formats the list for the Week view, adding date headers.
    private fun formatListForWeekView(tasks: List<Task>) {
        val formattedList = mutableListOf<TaskListItem>()
        // This logic groups the tasks by date to add the headers.
        tasks.sortedBy { it.dueDate }
            .groupBy { /* Some logic to get just the date part of task.dueDate */ }
            .forEach { (date, tasksOnThatDate) ->
                formattedList.add(TaskListItem.HeaderItem("Formatted Date Here"))
                tasksOnThatDate.forEach { task ->
                    formattedList.add(TaskListItem.WeekTaskItem(task))
                }
            }
        _displayList.value = formattedList
    }
}
package com.varsitycollege.schedulist.ui.main.simplelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.ui.adapter.TaskListItem

// This is the ViewModel for our main tasks screen. It fetches data
// from Firestore and gets it ready for our adapter. It uses a real-time
// listener, so the UI will update automatically if data changes in the database.

class TasksViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")

    // This holds the final, formatted list that our TasksAdapter will display.
    private val _displayList = MutableLiveData<List<TaskListItem>>()
    val displayList: LiveData<List<TaskListItem>> = _displayList

    // This holds the raw list of tasks we get directly from Firestore.
    private val _rawTasks = MutableLiveData<List<Task>>()

    // A variable to remember which view the user has selected.
    private var currentViewType = "Day"

    // We'll call this from our Fragment to start listening for data.
    fun startListeningForTasks(userId: String) {
        // .addSnapshotListener is the key for real-time updates. This code block
        // will run automatically every time the data changes in Firestore.
        tasksCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // We got new data! Convert all the documents into a list of our Task data class.
                    val tasks = snapshot.toObjects(Task::class.java)
                    _rawTasks.value = tasks
                    // Re-format the list based on the user's current view selection.
                    updateFormattedList()
                }
            }
    }

    // A public function the Fragment can call when the spinner changes.
    fun setViewType(viewType: String) {
        currentViewType = viewType
        updateFormattedList()
    }

    // A private helper that decides how to format the list.
    private fun updateFormattedList() {
        when (currentViewType) {
            "Day" -> formatListForDayView()
            "Week" -> formatListForWeekView()
            // "Month" would be handled by the MonthGridAdapter, so we might not format it here.
        }
    }

    private fun formatListForDayView() {
        _displayList.value = _rawTasks.value?.map { task ->
            TaskListItem.DayTaskItem(task)
        }
    }

    private fun formatListForWeekView() {
        val formattedList = mutableListOf<TaskListItem>()
        // This is a simple example of grouping tasks by date for the week view.
        _rawTasks.value?.sortedBy { it.dueDate }
            ?.groupBy { /* Some logic to get just the date part of task.dueDate */ }
            ?.forEach { (date, tasksOnThatDate) ->
                formattedList.add(TaskListItem.HeaderItem("Formatted Date Here"))
                tasksOnThatDate.forEach { task ->
                    formattedList.add(TaskListItem.WeekTaskItem(task))
                }
            }
        _displayList.value = formattedList
    }

    // This gets called from our "Add New Task" fragment.
    fun addTask(newTask: Task) {
        // Just add the new task object to the "tasks" collection in Firestore.
        // The snapshot listener will automatically pick up the change and update the UI.
        tasksCollection.add(newTask)
    }
}
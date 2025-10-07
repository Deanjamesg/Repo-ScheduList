package com.varsitycollege.schedulist.ui.main.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.ui.main.tasks.TasksViewModel

// This is the ViewModelFactory for our TasksViewModel.
// Its only job is to create our ViewModel and pass the repository into it.

class TasksViewModelFactory(private val repository: TasksRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
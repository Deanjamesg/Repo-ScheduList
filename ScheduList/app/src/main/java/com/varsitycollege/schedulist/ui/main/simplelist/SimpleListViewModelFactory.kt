package com.varsitycollege.schedulist.ui.main.simplelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.varsitycollege.schedulist.data.repository.SimpleListRepository

// The factory for our SimpleListViewModel. It's needed to pass the
// SimpleListRepository into the ViewModel when it's created.

class SimpleListViewModelFactory(private val repository: SimpleListRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimpleListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimpleListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
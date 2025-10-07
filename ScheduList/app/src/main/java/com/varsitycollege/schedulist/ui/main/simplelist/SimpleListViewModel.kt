package com.varsitycollege.schedulist.ui.main.simplelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.varsitycollege.schedulist.data.repository.SimpleListRepository
import com.varsitycollege.schedulist.ui.adapter.SimpleListItem

// This is the ViewModel for our 'Simple List' screen. Its only job is to
// hold the final combined and sorted list of items from the repository.

class SimpleListViewModel(private val repository: SimpleListRepository) : ViewModel() {

    // The LiveData that our Fragment will observe.
    val combinedList: LiveData<List<SimpleListItem>>

    init {
        // When the ViewModel is created, we ask the repository to start fetching all items.
        // We'd get the real userId from Firebase Auth here.
        val userId = "sampleUserId" // Placeholder
        combinedList = repository.getAllItems(userId)
    }

    // This function will be called from our Fragment when a checkbox is toggled.
    fun onItemCheckedChanged(item: SimpleListItem, isChecked: Boolean) {
        // TODO: Here you would call your repository to update the completion
        // status of the Task or Event in Firestore.
    }
}
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.varsitycollege.schedulist.data.repository.EventsRepository
import com.varsitycollege.schedulist.ui.main.events.EventsViewModel

// This is our ViewModelFactory. We need this so we can pass our
// repository into the ViewModel's constructor.

class EventsViewModelFactory(private val repository: EventsRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
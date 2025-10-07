package com.varsitycollege.schedulist.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.databinding.ItemDateHeaderBinding
import com.varsitycollege.schedulist.databinding.ItemTaskDayBinding
import com.varsitycollege.schedulist.databinding.ItemTaskWeekBinding
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

// Define integer constants for each of our layout types
private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_DAY = 1
private const val ITEM_VIEW_TYPE_WEEK = 2

// A sealed class to represent the different kinds of items our list can show
sealed class TaskListItem {
    data class DayTaskItem(val task: Task) : TaskListItem()
    data class WeekTaskItem(val task: Task) : TaskListItem()
    data class HeaderItem(val date: String) : TaskListItem()
}

class TasksAdapter(
    private val onTaskChecked: (Task) -> Unit
) : ListAdapter<TaskListItem, RecyclerView.ViewHolder>(TaskDiffCallback()) {

    inner class DayTaskViewHolder(private val binding: ItemTaskDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskListItem.DayTaskItem) {
            val task = item.task
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.tvTaskDate.text = dateFormatter.format(task.dueDate)
            binding.tvTaskTime.text = timeFormatter.format(task.dueDate)
            binding.cbTaskCompleted.isChecked = task.isCompleted

            // We set a click listener on the checkbox.
            binding.cbTaskCompleted.setOnClickListener {
                // When clicked, we call the function we passed in from the Fragment.
                onTaskChecked(task)
            }
        }
    }

    // This class holds the logic for a "Week" view item
    inner class WeekTaskViewHolder(private val binding: ItemTaskWeekBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskListItem.WeekTaskItem) {
            binding.tvTaskTitle.text = item.task.title

            // --- NEW: Format the Time and set the text ---
            binding.tvTaskTime.text = timeFormatter.format(item.task.dueDate)
        }
    }

    // This class holds the logic for a date header (e.g., "1 April")
    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskListItem.HeaderItem) {
            binding.tvDateHeader.text = item.date
        }
    }

    // This function tells the adapter which layout to use for a given item
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TaskListItem.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is TaskListItem.DayTaskItem -> ITEM_VIEW_TYPE_DAY
            is TaskListItem.WeekTaskItem -> ITEM_VIEW_TYPE_WEEK
        }
    }

    // This function creates the correct ViewHolder for the given view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> DateHeaderViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false))
            ITEM_VIEW_TYPE_DAY -> DayTaskViewHolder(ItemTaskDayBinding.inflate(inflater, parent, false))
            ITEM_VIEW_TYPE_WEEK -> WeekTaskViewHolder(ItemTaskWeekBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // This function connects the data from an item to its ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = getItem(position)) {
            is TaskListItem.HeaderItem -> (holder as DateHeaderViewHolder).bind(currentItem)
            is TaskListItem.DayTaskItem -> (holder as DayTaskViewHolder).bind(currentItem)
            is TaskListItem.WeekTaskItem -> (holder as WeekTaskViewHolder).bind(currentItem)
        }
    }
}

// The DiffUtil.ItemCallback that efficiently calculates list updates
class TaskDiffCallback : DiffUtil.ItemCallback<TaskListItem>() {
    override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        // You would typically compare unique IDs here
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        return oldItem == newItem
    }
}
package com.varsitycollege.schedulist.ui.adapter

import android.util.Log
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

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_DAY = 1
private const val ITEM_VIEW_TYPE_WEEK = 2

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

            // Handle null dueDate
            task.dueDate?.let {
                binding.tvTaskDate.text = dateFormatter.format(it)
                binding.tvTaskTime.text = timeFormatter.format(it)
            } ?: run {
                binding.tvTaskDate.text = "No due date"
                binding.tvTaskTime.text = ""
            }

            binding.cbTaskCompleted.isChecked = task.isCompleted

            binding.cbTaskCompleted.setOnClickListener {
                Log.d("TasksAdapter", "Checkbox clicked for task: ${task.title} (ID: ${task.id})")
                Log.d("TasksAdapter", "New state: ${binding.cbTaskCompleted.isChecked}")

                if (binding.cbTaskCompleted.isChecked) {
                    binding.tvTaskTitle.alpha = 0.5f
                    binding.tvTaskDescription.alpha = 0.5f
                } else {
                    binding.tvTaskTitle.alpha = 1.0f
                    binding.tvTaskDescription.alpha = 1.0f
                }
                onTaskChecked(task)
            }
        }
    }

    inner class WeekTaskViewHolder(private val binding: ItemTaskWeekBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskListItem.WeekTaskItem) {
            binding.tvTaskTitle.text = item.task.title

            // Handle null dueDate
            item.task.dueDate?.let {
                binding.tvTaskTime.text = timeFormatter.format(it)
            } ?: run {
                binding.tvTaskTime.text = "No time"
            }
        }
    }

    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskListItem.HeaderItem) {
            binding.tvDateHeader.text = item.date
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TaskListItem.HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is TaskListItem.DayTaskItem -> ITEM_VIEW_TYPE_DAY
            is TaskListItem.WeekTaskItem -> ITEM_VIEW_TYPE_WEEK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> DateHeaderViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false))
            ITEM_VIEW_TYPE_DAY -> DayTaskViewHolder(ItemTaskDayBinding.inflate(inflater, parent, false))
            ITEM_VIEW_TYPE_WEEK -> WeekTaskViewHolder(ItemTaskWeekBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = getItem(position)) {
            is TaskListItem.HeaderItem -> (holder as DateHeaderViewHolder).bind(currentItem)
            is TaskListItem.DayTaskItem -> (holder as DayTaskViewHolder).bind(currentItem)
            is TaskListItem.WeekTaskItem -> (holder as WeekTaskViewHolder).bind(currentItem)
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<TaskListItem>() {
    override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        return when {
            oldItem is TaskListItem.DayTaskItem && newItem is TaskListItem.DayTaskItem ->
                oldItem.task.id == newItem.task.id
            oldItem is TaskListItem.WeekTaskItem && newItem is TaskListItem.WeekTaskItem ->
                oldItem.task.id == newItem.task.id
            oldItem is TaskListItem.HeaderItem && newItem is TaskListItem.HeaderItem ->
                oldItem.date == newItem.date
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        // Checks if the content changed
        return when {
            oldItem is TaskListItem.DayTaskItem && newItem is TaskListItem.DayTaskItem -> {
                // Check if completion status changed
                oldItem.task.isCompleted == newItem.task.isCompleted &&
                        oldItem.task.title == newItem.task.title &&
                        oldItem.task.description == newItem.task.description
            }
            oldItem is TaskListItem.WeekTaskItem && newItem is TaskListItem.WeekTaskItem -> {
                oldItem.task.isCompleted == newItem.task.isCompleted &&
                        oldItem.task.title == newItem.task.title
            }
            oldItem is TaskListItem.HeaderItem && newItem is TaskListItem.HeaderItem -> {
                oldItem.date == newItem.date
            }
            else -> false
        }
    }
}
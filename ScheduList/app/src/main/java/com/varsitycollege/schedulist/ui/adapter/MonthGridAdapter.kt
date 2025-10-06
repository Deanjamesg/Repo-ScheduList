package com.varsitycollege.schedulist.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.databinding.ItemTaskMonthBinding

// A simple data class to represent one day in the grid
data class MonthDay(
    val dayOfMonth: String, // e.g., "1", "2", or "" for empty cells
    val tasks: List<Task>
)

class MonthGridAdapter : ListAdapter<MonthDay, MonthGridAdapter.MonthDayViewHolder>(MonthDayDiffCallback()) {

    inner class MonthDayViewHolder(private val binding: ItemTaskMonthBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: MonthDay) {
            binding.tvDayNumber.text = day.dayOfMonth
            // Create a formatted string from the list of tasks
            val tasksText = day.tasks.joinToString("\n") { "• ${it.title}" }
            binding.tvTasks.text = tasksText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthDayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskMonthBinding.inflate(inflater, parent, false)
        return MonthDayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthDayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MonthDayDiffCallback : DiffUtil.ItemCallback<MonthDay>() {
    override fun areItemsTheSame(oldItem: MonthDay, newItem: MonthDay): Boolean {
        return oldItem.dayOfMonth == newItem.dayOfMonth
    }

    override fun areContentsTheSame(oldItem: MonthDay, newItem: MonthDay): Boolean {
        return oldItem == newItem
    }
}
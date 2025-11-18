package com.varsitycollege.schedulist.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.databinding.ItemTaskMonthBinding

data class MonthDay(
    val dayOfMonth: String,
    val tasks: List<Task>
)

// Added the click listener to the constructor here
class MonthGridAdapter(
    private val onDayClick: ((MonthDay) -> Unit)? = null
) : ListAdapter<MonthDay, MonthGridAdapter.MonthDayViewHolder>(MonthDayDiffCallback()) {

    inner class MonthDayViewHolder(private val binding: ItemTaskMonthBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: MonthDay) {
            binding.tvDayNumber.text = day.dayOfMonth
            val tasksText = day.tasks.joinToString("\n") { "• ${it.title}" }
            binding.tvTasks.text = tasksText

            // Only set the listener if one was actually passed in
            binding.root.setOnClickListener {
                onDayClick?.invoke(day)
            }
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
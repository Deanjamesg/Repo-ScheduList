package com.varsitycollege.schedulist.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.schedulist.data.model.Task
import com.varsitycollege.schedulist.databinding.ItemSimpleListBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarTaskAdapter : ListAdapter<Task, CalendarTaskAdapter.CalendarTaskViewHolder>(DiffCallback()) {

    private val dateFormatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    inner class CalendarTaskViewHolder(private val binding: ItemSimpleListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDateTime.text = dateFormatter.format(task.dueDate)

            // We just show the checkbox state, but we don't need it to be clickable here
            binding.checkBoxSimple.isChecked = task.isCompleted
            binding.checkBoxSimple.isEnabled = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarTaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSimpleListBinding.inflate(inflater, parent, false)
        return CalendarTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
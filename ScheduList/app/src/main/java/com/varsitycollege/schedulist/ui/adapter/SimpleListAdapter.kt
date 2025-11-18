package com.varsitycollege.schedulist.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.schedulist.databinding.ItemSimpleListBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// A simple formatter for the date and time.
private val simpleListDateFormatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

// We need to add 'isCompleted' to our data class to track the checkbox state.
data class SimpleListItem(
    val id: String,
    val title: String,
    val date: Date?,
    val isCompleted: Boolean
)

class SimpleListAdapter(
    private val onItemChecked: (SimpleListItem, Boolean) -> Unit
) : ListAdapter<SimpleListItem, SimpleListAdapter.SimpleListViewHolder>(SimpleListDiffCallback()) {

    // The ViewHolder now binds the title, date, and the checkbox's status.
    inner class SimpleListViewHolder(private val binding: ItemSimpleListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimpleListItem) {
            binding.tvTitle.text = item.title
            binding.tvDateTime.text = simpleListDateFormatter.format(item.date)
            binding.checkBoxSimple.isChecked = item.isCompleted

            // This makes the checkbox interactive.
            binding.checkBoxSimple.setOnCheckedChangeListener { _, isChecked ->
                onItemChecked(item, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSimpleListBinding.inflate(inflater, parent, false)
        return SimpleListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimpleListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class SimpleListDiffCallback : DiffUtil.ItemCallback<SimpleListItem>() {
    override fun areItemsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
        return oldItem == newItem
    }
}
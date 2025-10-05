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

// A generic data class for our simple list. This lets us show both Tasks
// and Events in the same list by converting them to this simpler object first.
data class SimpleListItem(
    val id: String,
    val title: String,
    val date: Date
)


class SimpleListAdapter : ListAdapter<SimpleListItem, SimpleListAdapter.SimpleListViewHolder>(SimpleListDiffCallback()) {

    // This is our ViewHolder. It just holds onto the views from item_simple_list.xml.
    inner class SimpleListViewHolder(private val binding: ItemSimpleListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimpleListItem) {
            binding.tvTitle.text = item.title
            binding.tvTitle.text = simpleListDateFormatter.format(item.date)
        }
    }

    // Creates an empty ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSimpleListBinding.inflate(inflater, parent, false)
        return SimpleListViewHolder(binding)
    }

    // Fills the ViewHolder with data.
    override fun onBindViewHolder(holder: SimpleListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

// DiffUtil for efficient list updates.
class SimpleListDiffCallback : DiffUtil.ItemCallback<SimpleListItem>() {
    override fun areItemsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
        return oldItem == newItem
    }
}
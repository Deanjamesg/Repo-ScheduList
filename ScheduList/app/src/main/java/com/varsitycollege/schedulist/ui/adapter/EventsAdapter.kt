package com.varsitycollege.schedulist.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.databinding.ItemEventDayBinding
import com.varsitycollege.schedulist.databinding.ItemEventWeekBinding
import java.text.SimpleDateFormat
import java.util.Locale

// These are our formatters to turn a Date object into text like "Apr 1, 2025" or "9:41 AM"
private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

// This sealed class defines what kind of items our list can actually show
sealed class EventListItem {
    data class DayEventItem(val event: Event) : EventListItem()
    data class WeekEventItem(val event: Event) : EventListItem()
}

class EventsAdapter : ListAdapter<EventListItem, RecyclerView.ViewHolder>(EventDiffCallback()) {

    // These constants represent our two different layouts.
    companion object {
        private const val ITEM_VIEW_TYPE_DAY = 1
        private const val ITEM_VIEW_TYPE_WEEK = 2
    }

    // The ViewHolder for our detailed 'Day' view layout.
    inner class DayEventViewHolder(private val binding: ItemEventDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventListItem.DayEventItem) {
            val event = item.event
            binding.tvEventTitle.text = event.title
            binding.tvEventDescription.text = event.description
            binding.tvEventLocation.text = event.location
            binding.chipDate.text = dateFormatter.format(event.startTime)
            binding.chipTime.text = timeFormatter.format(event.startTime)
        }
    }

    // The ViewHolder for our compact 'Week' view layout.
    inner class WeekEventViewHolder(private val binding: ItemEventWeekBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventListItem.WeekEventItem) {
            val event = item.event
            binding.tvEventTitle.text = event.title
            binding.tvEventLocation.text = event.location
            binding.tvEventTime.text = timeFormatter.format(event.startTime)
        }
    }

    // This function checks the item type and returns the correct layout constant.
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EventListItem.DayEventItem -> ITEM_VIEW_TYPE_DAY
            is EventListItem.WeekEventItem -> ITEM_VIEW_TYPE_WEEK
        }
    }

    // This function creates the correct (but empty) ViewHolder for the view type.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_DAY -> DayEventViewHolder(ItemEventDayBinding.inflate(inflater, parent, false))
            ITEM_VIEW_TYPE_WEEK -> WeekEventViewHolder(ItemEventWeekBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // This function takes an empty ViewHolder and fills it with the actual data.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = getItem(position)) {
            is EventListItem.DayEventItem -> (holder as DayEventViewHolder).bind(currentItem)
            is EventListItem.WeekEventItem -> (holder as WeekEventViewHolder).bind(currentItem)
        }
    }
}

// This is the DiffUtil part that makes the ListAdapter efficient.
class EventDiffCallback : DiffUtil.ItemCallback<EventListItem>() {
    override fun areItemsTheSame(oldItem: EventListItem, newItem: EventListItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: EventListItem, newItem: EventListItem): Boolean {
        return oldItem == newItem
    }
}
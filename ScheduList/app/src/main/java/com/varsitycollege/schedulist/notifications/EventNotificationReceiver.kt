package com.varsitycollege.schedulist.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.varsitycollege.schedulist.MainActivity
import com.varsitycollege.schedulist.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventNotificationReceiver : BroadcastReceiver() {

    private val TAG = "EventNotificationReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Notification received!")

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: return
        val eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: "Event"
        val eventStartTime = intent.getLongExtra(EXTRA_EVENT_START_TIME, 0L)
        val eventEndTime = intent.getLongExtra(EXTRA_EVENT_END_TIME, 0L)
        val daysUntilEvent = intent.getIntExtra(EXTRA_DAYS_UNTIL_EVENT, -1)

        Log.d(TAG, "Event: $eventTitle, ID: $eventId, Days until: $daysUntilEvent")

        // Check if event reminders are enabled
        val sharedPreferences = context.getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        val eventRemindersEnabled = sharedPreferences.getBoolean("event_reminders", true)

        if (!eventRemindersEnabled) {
            Log.d(TAG, "Event reminders disabled in settings")
            return
        }

        val notificationMessage = buildNotificationMessage(eventStartTime, eventEndTime, daysUntilEvent)
        Log.d(TAG, "Showing notification: $notificationMessage")

        showNotification(context, eventId, eventTitle, notificationMessage)
    }

    private fun buildNotificationMessage(startTime: Long, endTime: Long, daysUntilEvent: Int): String {
        val now = System.currentTimeMillis()
        val startDate = Date(startTime)
        val endDate = Date(endTime)
        val currentDate = Date(now)

        // If daysUntilEvent is provided (countdown notification)
        if (daysUntilEvent >= 0) {
            return when (daysUntilEvent) {
                0 -> "Today at ${formatTime(startDate)}"
                1 -> "Tomorrow at ${formatTime(startDate)}"
                else -> "$daysUntilEvent days until - ${formatDate(startDate)} at ${formatTime(startDate)}"
            }
        }

        // Check if event is ongoing (for multi-day events)
        if (currentDate.after(startDate) && currentDate.before(endDate)) {
            // Event is ongoing - calculate which day
            val totalDuration = endTime - startTime
            val totalDays = (totalDuration / (1000 * 60 * 60 * 24)).toInt()
            val elapsedTime = now - startTime
            val currentDay = (elapsedTime / (1000 * 60 * 60 * 24)).toInt() + 1

            if (totalDays > 0) {
                return "Ongoing (Day $currentDay of ${totalDays + 1}) - Ends ${formatDateTime(endDate)}"
            } else {
                return "Ongoing - Ends ${formatTime(endDate)}"
            }
        } else {
            // Event is upcoming
            return formatDateTime(startDate)
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return sdf.format(date)
    }

    private fun formatDateTime(date: Date): String {
        val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    private fun formatTime(date: Date): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    private fun showNotification(context: Context, eventId: String, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming and ongoing events"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("eventId", eventId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(eventId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "event_reminders_channel"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_TITLE = "extra_event_title"
        const val EXTRA_EVENT_START_TIME = "extra_event_start_time"
        const val EXTRA_EVENT_END_TIME = "extra_event_end_time"
        const val EXTRA_DAYS_UNTIL_EVENT = "extra_days_until_event"
    }
}


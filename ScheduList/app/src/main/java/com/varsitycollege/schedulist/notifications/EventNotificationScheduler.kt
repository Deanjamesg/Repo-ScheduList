package com.varsitycollege.schedulist.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.varsitycollege.schedulist.data.model.Event
import com.varsitycollege.schedulist.data.model.ReminderType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EventNotificationScheduler {

    private const val TAG = "EventNotificationScheduler"
    private val timeFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())

    fun scheduleNotification(context: Context, event: Event) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📅 SCHEDULING NOTIFICATION FOR EVENT")
        Log.d(TAG, "Event: ${event.title}")
        Log.d(TAG, "Start Time: ${timeFormat.format(event.startTime)}")
        Log.d(TAG, "Reminder Type: ${event.reminderType}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        if (event.reminderType == ReminderType.NONE) {
            Log.d(TAG, "❌ No reminder set for event: ${event.title}")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if app can schedule exact alarms (required for Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌❌❌ CRITICAL: Cannot schedule exact alarms!")
                Log.e(TAG, "Permission not granted. Notifications WILL NOT WORK!")
                Log.e(TAG, "Fix: Settings → Apps → ScheduList → Alarms & reminders → Enable")
                Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                return
            } else {
                Log.d(TAG, "✅ Exact alarm permission granted")
            }
        }

        val scheduledTimes = mutableListOf<Date>()

        // For SEVEN_DAYS_BEFORE reminder, schedule daily countdown notifications
        when (event.reminderType) {
            ReminderType.SEVEN_DAYS_BEFORE -> {
                Log.d(TAG, "📢 Scheduling 7-day countdown notifications...")

                // Calculate how many days until the event
                val now = Calendar.getInstance()
                val eventCal = Calendar.getInstance().apply { time = event.startTime }
                val daysUntilEvent = ((eventCal.timeInMillis - now.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

                Log.d(TAG, "Event is $daysUntilEvent days away")

                // If event is within 7 days, send immediate notification for current countdown
                if (daysUntilEvent in 0..7) {
                    // Check if 9 AM has already passed today
                    val nineAmToday = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 9)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (now.after(nineAmToday)) {
                        // 9 AM has passed, send immediate notification
                        Log.d(TAG, "Event is within 7 days and 9 AM passed - sending immediate notification")
                        val time = scheduleImmediateCountdownNotification(context, event, daysUntilEvent, alarmManager)
                        if (time != null) scheduledTimes.add(time)
                    }
                }

                // Schedule future notifications (0 to 7 days before)
                for (daysAway in 7 downTo 0) {
                    val time = scheduleCountdownNotification(context, event, daysAway, alarmManager)
                    if (time != null) scheduledTimes.add(time)
                }
            }
            ReminderType.DAY_OF -> {
                Log.d(TAG, "📢 Scheduling day-of notification...")
                // Only schedule day-of notification
                val time = scheduleCountdownNotification(context, event, 0, alarmManager)
                if (time != null) scheduledTimes.add(time)
            }
            else -> {
                Log.d(TAG, "⚠️ Reminder type not handled: ${event.reminderType}")
            }
        }

        // Also schedule daily ongoing event notifications if it's a multi-day event
        if (event.endTime != null) {
            Log.d(TAG, "📢 Scheduling ongoing event notifications...")
            scheduleDailyOngoingEventCheck(context, event, alarmManager)
        }

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "✅ NOTIFICATION SCHEDULING COMPLETED")
        Log.d(TAG, "Total notifications scheduled: ${scheduledTimes.size}")
        scheduledTimes.forEachIndexed { index, time ->
            Log.d(TAG, "  ${index + 1}. ${timeFormat.format(time)}")
        }
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun scheduleCountdownNotification(
        context: Context,
        event: Event,
        daysAway: Int,
        alarmManager: AlarmManager
    ): Date? {
        val notificationTime = calculateNotificationTime(event.startTime, daysAway)
        val now = Date()

        // Don't schedule if notification time is in the past
        if (notificationTime.before(now)) {
            Log.w(TAG, "  ⏭️ Skipping (past): ${daysAway} days away - ${timeFormat.format(notificationTime)}")
            return null
        }

        Log.d(TAG, "  ⏰ Scheduling: ${daysAway} days away at ${timeFormat.format(notificationTime)}")

        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            putExtra(EventNotificationReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(EventNotificationReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(EventNotificationReceiver.EXTRA_EVENT_START_TIME, event.startTime.time)
            putExtra(EventNotificationReceiver.EXTRA_EVENT_END_TIME, event.endTime?.time ?: event.startTime.time)
            putExtra(EventNotificationReceiver.EXTRA_DAYS_UNTIL_EVENT, daysAway)
        }

        val requestCode = (event.id.hashCode() + daysAway * 1000)
        Log.d(TAG, "     Request Code: $requestCode")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime.time,
                pendingIntent
            )
            Log.d(TAG, "     ✅ Scheduled successfully")
            return notificationTime
        } catch (e: SecurityException) {
            Log.e(TAG, "     ❌ Failed to schedule: ${e.message}")
            return null
        }
    }

    private fun calculateNotificationTime(eventTime: Date, daysBeforeEvent: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = eventTime

        // Subtract the days
        calendar.add(Calendar.DAY_OF_YEAR, -daysBeforeEvent)

        // Set to 9 AM
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // SPECIAL CASE: If this is a day-of notification (daysBeforeEvent == 0)
        // and 9 AM has already passed today, schedule for immediately (1 second from now)
        val now = Date()
        if (daysBeforeEvent == 0 && calendar.time.before(now)) {
            Log.d(TAG, "     ⚠️ 9 AM has passed for today - scheduling notification immediately")
            val immediate = Calendar.getInstance()
            immediate.add(Calendar.SECOND, 1)
            return immediate.time
        }

        return calendar.time
    }

    private fun scheduleImmediateCountdownNotification(
        context: Context,
        event: Event,
        daysUntilEvent: Int,
        alarmManager: AlarmManager
    ): Date? {
        Log.d(TAG, "  ⚡ Scheduling IMMEDIATE notification for $daysUntilEvent days until event")

        val immediate = Calendar.getInstance()
        immediate.add(Calendar.SECOND, 1)

        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            putExtra(EventNotificationReceiver.EXTRA_EVENT_ID, event.id + "_immediate")
            putExtra(EventNotificationReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(EventNotificationReceiver.EXTRA_EVENT_START_TIME, event.startTime.time)
            putExtra(EventNotificationReceiver.EXTRA_EVENT_END_TIME, event.endTime?.time ?: event.startTime.time)
            putExtra(EventNotificationReceiver.EXTRA_DAYS_UNTIL_EVENT, daysUntilEvent)
        }

        val requestCode = (event.id.hashCode() + 9000) // Different offset for immediate notification
        Log.d(TAG, "     Request Code: $requestCode")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                immediate.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "     ✅ Immediate notification scheduled successfully")
            return immediate.time
        } catch (e: SecurityException) {
            Log.e(TAG, "     ❌ Failed to schedule immediate notification: ${e.message}")
            return null
        }
    }

    private fun scheduleDailyOngoingEventCheck(context: Context, event: Event, alarmManager: AlarmManager) {
        val startDate = event.startTime
        val endDate = event.endTime ?: return

        // Schedule daily notifications at 9 AM for each day of the event (from start to end)
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        var dayCount = 0
        while (calendar.time.before(endDate) || isSameDay(calendar.time, endDate)) {
            if (calendar.time.after(Date())) {
                val intent = Intent(context, EventNotificationReceiver::class.java).apply {
                    putExtra(EventNotificationReceiver.EXTRA_EVENT_ID, event.id + "_ongoing_$dayCount")
                    putExtra(EventNotificationReceiver.EXTRA_EVENT_TITLE, event.title)
                    putExtra(EventNotificationReceiver.EXTRA_EVENT_START_TIME, event.startTime.time)
                    putExtra(EventNotificationReceiver.EXTRA_EVENT_END_TIME, endDate.time)
                    putExtra(EventNotificationReceiver.EXTRA_DAYS_UNTIL_EVENT, -1) // -1 indicates ongoing event notification
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (event.id.hashCode() + 10000 + dayCount), // Different offset to avoid conflicts with countdown notifications
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Ongoing event notification scheduled for day $dayCount at ${calendar.time}")
                } catch (e: SecurityException) {
                    Log.e(TAG, "Failed to schedule ongoing event notification: ${e.message}")
                }
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dayCount++
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun cancelNotification(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel countdown notifications (0-7 days)
        for (daysAway in 0..7) {
            val intent = Intent(context, EventNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (eventId.hashCode() + daysAway * 1000),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        // Cancel ongoing event notifications (up to 30 days)
        for (dayCount in 0..30) {
            val intent = Intent(context, EventNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (eventId.hashCode() + 10000 + dayCount),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        Log.d(TAG, "All notifications cancelled for event: $eventId")
    }
}


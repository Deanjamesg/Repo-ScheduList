package com.varsitycollege.schedulist.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.varsitycollege.schedulist.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility to test and debug notification functionality
 */
object NotificationDebugHelper {

    private const val TAG = "NotificationDebug"

    /**
     * Check all notification-related permissions and settings
     */
    fun checkNotificationStatus(context: Context): String {
        val report = StringBuilder()
        report.appendLine("=== NOTIFICATION DEBUG REPORT ===")
        report.appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        report.appendLine()

        // Check Android version
        report.appendLine("📱 Device Info:")
        report.appendLine("  Android Version: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
        report.appendLine("  Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        report.appendLine()

        // Check notification permission (Android 13+)
        report.appendLine("🔔 Notification Permission:")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val hasPermission = notificationManager.areNotificationsEnabled()
            report.appendLine(if (hasPermission) "  ✅ GRANTED" else "  ❌ DENIED - Grant in Settings → Apps → ScheduList → Permissions")
        } else {
            report.appendLine("  ✅ Not required (Android < 13)")
        }
        report.appendLine()

        // Check exact alarm permission (Android 12+)
        report.appendLine("⏰ Exact Alarm Permission:")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canSchedule = alarmManager.canScheduleExactAlarms()
            report.appendLine(if (canSchedule) "  ✅ GRANTED" else "  ❌ DENIED - Enable in Settings → Apps → ScheduList → Alarms & reminders")
            if (!canSchedule) {
                report.appendLine("  ⚠️  THIS IS CRITICAL! Notifications WILL NOT work without this!")
            }
        } else {
            report.appendLine("  ✅ Not required (Android < 12)")
        }
        report.appendLine()

        // Check notification channel
        report.appendLine("📢 Notification Channel:")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel("event_reminders_channel")
            if (channel != null) {
                report.appendLine("  ✅ Channel exists: ${channel.name}")
                report.appendLine("  Importance: ${channel.importance}")
                report.appendLine("  Sound: ${if (channel.sound != null) "Enabled" else "Disabled"}")
            } else {
                report.appendLine("  ⚠️  Channel not created yet")
            }
        } else {
            report.appendLine("  ✅ Not required (Android < 8)")
        }
        report.appendLine()

        // Check app notification settings
        report.appendLine("⚙️ App Settings:")
        val sharedPreferences = context.getSharedPreferences("settings_preferences", Context.MODE_PRIVATE)
        val eventRemindersEnabled = sharedPreferences.getBoolean("event_reminders", true)
        report.appendLine(if (eventRemindersEnabled) "  ✅ Event Reminders: Enabled" else "  ❌ Event Reminders: DISABLED in app settings")
        report.appendLine()

        // Overall status
        report.appendLine("🎯 Overall Status:")
        val allGood = (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationManager.areNotificationsEnabled()) &&
                      (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) &&
                      eventRemindersEnabled

        if (allGood) {
            report.appendLine("  ✅ ALL CHECKS PASSED - Notifications should work!")
        } else {
            report.appendLine("  ❌ ISSUES FOUND - Fix the items marked with ❌ above")
        }

        report.appendLine()
        report.appendLine("=== END REPORT ===")

        val reportText = report.toString()
        Log.d(TAG, reportText)
        return reportText
    }

    /**
     * Send a test notification immediately to verify notification display works
     */
    fun sendTestNotification(context: Context) {
        Log.d(TAG, "Sending test notification...")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test_channel",
                "Test Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Test notifications to verify functionality"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build and show test notification
        val notification = NotificationCompat.Builder(context, "test_channel")
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle("🎉 Test Notification")
            .setContentText("If you see this, notifications are working!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("This is a test notification sent at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())}. If you can see this, your notification system is working correctly!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(99999, notification)
        Log.d(TAG, "✅ Test notification sent! Check your notification panel.")
    }

    /**
     * Log all information about an event's notification scheduling
     */
    fun logEventScheduling(
        eventTitle: String,
        eventStartTime: Date,
        reminderType: String,
        notificationTimes: List<Date>
    ) {
        Log.d(TAG, "=== Event Notification Schedule ===")
        Log.d(TAG, "Event: $eventTitle")
        Log.d(TAG, "Start Time: ${SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(eventStartTime)}")
        Log.d(TAG, "Reminder Type: $reminderType")
        Log.d(TAG, "Scheduled Notifications (${notificationTimes.size}):")
        notificationTimes.forEachIndexed { index, time ->
            val isPast = time.before(Date())
            val status = if (isPast) "⚠️ PAST (won't trigger)" else "✓ Future"
            Log.d(TAG, "  ${index + 1}. ${SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(time)} - $status")
        }
        Log.d(TAG, "===================================")
    }
}


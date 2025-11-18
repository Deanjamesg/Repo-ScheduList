package com.varsitycollege.schedulist.notifications

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object NotificationPermissionHelper {

    private const val TAG = "NotificationPermHelper"
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    fun hasNotificationPermission(activity: Activity): Boolean {
        val has = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        Log.d(TAG, "hasNotificationPermission=${has} (SDK=${Build.VERSION.SDK_INT})")
        return has
    }

    fun requestNotificationPermission(activity: Activity) {
        Log.d(TAG, "Requesting notification permission (SDK=${Build.VERSION.SDK_INT})")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        Log.d(TAG, "canScheduleExactAlarms=$result (SDK=${Build.VERSION.SDK_INT})")
        return result
    }

    fun requestExactAlarmPermission(activity: Activity) {
        Log.d(TAG, "Requesting exact alarm permission UI (SDK=${Build.VERSION.SDK_INT})")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        }
    }

    fun hasAllPermissions(activity: Activity): Boolean {
        val all = hasNotificationPermission(activity) && canScheduleExactAlarms(activity)
        Log.d(TAG, "hasAllPermissions=$all")
        return all
    }
}

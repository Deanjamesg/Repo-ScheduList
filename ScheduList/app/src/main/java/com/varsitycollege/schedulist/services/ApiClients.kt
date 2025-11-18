package com.varsitycollege.schedulist.services

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ApiClients {
    var calendarApi: CalendarApiClient? = null
    var tasksApi: TasksApiClient? = null

    fun initialize(context: Context, userEmail: String) {
        calendarApi = CalendarApiClient(context.applicationContext, userEmail)
        tasksApi = TasksApiClient(context.applicationContext, userEmail)
    }

    fun clear() {
        calendarApi?.clearCache()
        tasksApi?.clearCache()
        calendarApi = null
        tasksApi = null

        // Reset sync manager on logout
        SyncManager.reset()
    }
}
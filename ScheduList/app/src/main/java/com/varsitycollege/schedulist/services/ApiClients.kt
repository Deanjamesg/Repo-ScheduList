package com.varsitycollege.schedulist.services

import android.content.Context

object ApiClients {
    var calendarApi: CalendarApiClient? = null
    var tasksApi: TasksApiClient? = null

    fun initialize(context: Context, userEmail: String) {
        calendarApi = CalendarApiClient(context.applicationContext, userEmail)
        tasksApi = TasksApiClient(context.applicationContext, userEmail)
    }

    fun clear() {
        calendarApi?.clearCache()
        calendarApi = null
        tasksApi = null
    }
}
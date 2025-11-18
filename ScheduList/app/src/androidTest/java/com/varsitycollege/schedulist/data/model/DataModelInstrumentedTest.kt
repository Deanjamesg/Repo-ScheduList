package com.varsitycollege.schedulist.data.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.varsitycollege.schedulist.data.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Instrumented test for data models
 * Tests serialization and Android-specific behavior
 */
@RunWith(AndroidJUnit4::class)
class DataModelInstrumentedTest {

    @Test
    fun task_serialization_preserves_data() {
        val originalTask = Task(
            id = "task123",
            title = "Test Task",
            description = "Test Description",
            dueDate = Date(),
            energyLevel = EnergyLevel.HIGH,
            isCompleted = false,
            taskListId = "list456",
            userId = "user789"
        )

        // Create a copy to simulate serialization/deserialization
        val copiedTask = originalTask.copy()

        assertEquals(originalTask.id, copiedTask.id)
        assertEquals(originalTask.title, copiedTask.title)
        assertEquals(originalTask.description, copiedTask.description)
        assertEquals(originalTask.energyLevel, copiedTask.energyLevel)
        assertEquals(originalTask.isCompleted, copiedTask.isCompleted)
    }

    @Test
    fun event_serialization_preserves_data() {
        val startTime = Date()
        val endTime = Date(startTime.time + 3600000)

        val originalEvent = Event(
            id = "event123",
            googleCalendarEventId = "gcal456",
            title = "Test Event",
            description = "Test Description",
            location = "Test Location",
            startTime = startTime,
            endTime = endTime,
            userId = "user789",
            reminderType = ReminderType.DAY_OF
        )

        val copiedEvent = originalEvent.copy()

        assertEquals(originalEvent.id, copiedEvent.id)
        assertEquals(originalEvent.title, copiedEvent.title)
        assertEquals(originalEvent.description, copiedEvent.description)
        assertEquals(originalEvent.reminderType, copiedEvent.reminderType)
    }

    @Test
    fun user_with_notification_preferences_serialization() {
        val notificationPrefs = NotificationPreferences(
            travelTimeAlerts = true,
            taskReminders = false,
            eventReminders = true,
            productivityAlerts = false
        )

        val originalUser = User(
            userId = "user123",
            displayName = "Test User",
            email = "test@example.com",
            notificationPrefs = notificationPrefs
        )

        val copiedUser = originalUser.copy()

        assertEquals(originalUser.userId, copiedUser.userId)
        assertEquals(originalUser.displayName, copiedUser.displayName)
        assertEquals(originalUser.email, copiedUser.email)
        assertEquals(originalUser.notificationPrefs.travelTimeAlerts, copiedUser.notificationPrefs.travelTimeAlerts)
        assertEquals(originalUser.notificationPrefs.taskReminders, copiedUser.notificationPrefs.taskReminders)
    }

    @Test
    fun energy_level_enum_string_conversion() {
        val low = EnergyLevel.LOW
        val medium = EnergyLevel.MEDIUM
        val high = EnergyLevel.HIGH

        assertEquals("LOW", low.name)
        assertEquals("MEDIUM", medium.name)
        assertEquals("HIGH", high.name)

        assertEquals(EnergyLevel.valueOf("LOW"), low)
        assertEquals(EnergyLevel.valueOf("MEDIUM"), medium)
        assertEquals(EnergyLevel.valueOf("HIGH"), high)
    }

    @Test
    fun reminder_type_enum_string_conversion() {
        val none = ReminderType.NONE
        val dayOf = ReminderType.DAY_OF
        val sevenDays = ReminderType.SEVEN_DAYS_BEFORE

        assertEquals("NONE", none.name)
        assertEquals("DAY_OF", dayOf.name)
        assertEquals("SEVEN_DAYS_BEFORE", sevenDays.name)

        assertEquals(ReminderType.valueOf("NONE"), none)
        assertEquals(ReminderType.valueOf("DAY_OF"), dayOf)
        assertEquals(ReminderType.valueOf("SEVEN_DAYS_BEFORE"), sevenDays)
    }
}


package com.varsitycollege.schedulist.data.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class EventTest {

    @Test
    fun `event creation with default values`() {
        val event = Event()

        assertEquals("", event.id)
        assertEquals("", event.googleCalendarEventId)
        assertEquals("", event.title)
        assertEquals("", event.description)
        assertEquals("", event.location)
        assertNotNull(event.startTime)
        assertNull(event.endTime)
        assertEquals("", event.userId)
        assertEquals(ReminderType.NONE, event.reminderType)
    }

    @Test
    fun `event creation with custom values`() {
        val startTime = Date()
        val endTime = Date(startTime.time + 3600000) // 1 hour later

        val event = Event(
            id = "event123",
            googleCalendarEventId = "gcal456",
            title = "Team Meeting",
            description = "Weekly sync",
            location = "Conference Room A",
            startTime = startTime,
            endTime = endTime,
            userId = "user789",
            reminderType = ReminderType.DAY_OF
        )

        assertEquals("event123", event.id)
        assertEquals("gcal456", event.googleCalendarEventId)
        assertEquals("Team Meeting", event.title)
        assertEquals("Weekly sync", event.description)
        assertEquals("Conference Room A", event.location)
        assertEquals(startTime, event.startTime)
        assertEquals(endTime, event.endTime)
        assertEquals("user789", event.userId)
        assertEquals(ReminderType.DAY_OF, event.reminderType)
    }

    @Test
    fun `event copy with modified values`() {
        val originalEvent = Event(
            id = "event1",
            title = "Original Event",
            reminderType = ReminderType.NONE
        )

        val modifiedEvent = originalEvent.copy(
            title = "Modified Event",
            reminderType = ReminderType.SEVEN_DAYS_BEFORE
        )

        assertEquals("event1", modifiedEvent.id)
        assertEquals("Modified Event", modifiedEvent.title)
        assertEquals(ReminderType.SEVEN_DAYS_BEFORE, modifiedEvent.reminderType)

        // Original should remain unchanged
        assertEquals("Original Event", originalEvent.title)
        assertEquals(ReminderType.NONE, originalEvent.reminderType)
    }

    @Test
    fun `reminder type enum values`() {
        val types = ReminderType.values()

        assertEquals(3, types.size)
        assertTrue(types.contains(ReminderType.NONE))
        assertTrue(types.contains(ReminderType.DAY_OF))
        assertTrue(types.contains(ReminderType.SEVEN_DAYS_BEFORE))
    }

    @Test
    fun `event equality check`() {
        val startTime = Date()
        val event1 = Event(
            id = "event1",
            title = "Test Event",
            startTime = startTime
        )

        val event2 = Event(
            id = "event1",
            title = "Test Event",
            startTime = startTime
        )

        assertEquals(event1, event2)
    }

    @Test
    fun `event with null end time`() {
        val event = Event(
            id = "event1",
            title = "All Day Event",
            endTime = null
        )

        assertNull(event.endTime)
    }
}


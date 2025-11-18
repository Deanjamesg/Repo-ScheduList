package com.varsitycollege.schedulist.data.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {

    @Test
    fun `user creation with default values`() {
        val user = User()

        assertEquals("", user.userId)
        assertNull(user.displayName)
        assertNull(user.email)
        assertNull(user.profilePictureUrl)
        assertNull(user.scheduListCalendarId)
        assertNotNull(user.notificationPrefs)
        assertTrue(user.createdAt > 0)
    }

    @Test
    fun `user creation with custom values`() {
        val createdAt = System.currentTimeMillis()
        val user = User(
            userId = "user123",
            displayName = "John Doe",
            email = "john@example.com",
            profilePictureUrl = "https://example.com/photo.jpg",
            scheduListCalendarId = "calendar456",
            createdAt = createdAt
        )

        assertEquals("user123", user.userId)
        assertEquals("John Doe", user.displayName)
        assertEquals("john@example.com", user.email)
        assertEquals("https://example.com/photo.jpg", user.profilePictureUrl)
        assertEquals("calendar456", user.scheduListCalendarId)
        assertEquals(createdAt, user.createdAt)
    }

    @Test
    fun `user with custom notification preferences`() {
        val notificationPrefs = NotificationPreferences(
            travelTimeAlerts = false,
            taskReminders = true,
            eventReminders = false,
            productivityAlerts = true
        )

        val user = User(
            userId = "user123",
            notificationPrefs = notificationPrefs
        )

        assertFalse(user.notificationPrefs.travelTimeAlerts)
        assertTrue(user.notificationPrefs.taskReminders)
        assertFalse(user.notificationPrefs.eventReminders)
        assertTrue(user.notificationPrefs.productivityAlerts)
    }

    @Test
    fun `user copy with modified values`() {
        val originalUser = User(
            userId = "user1",
            displayName = "Original Name",
            email = "original@example.com"
        )

        val modifiedUser = originalUser.copy(
            displayName = "Modified Name",
            email = "modified@example.com"
        )

        assertEquals("user1", modifiedUser.userId)
        assertEquals("Modified Name", modifiedUser.displayName)
        assertEquals("modified@example.com", modifiedUser.email)

        // Original should remain unchanged
        assertEquals("Original Name", originalUser.displayName)
        assertEquals("original@example.com", originalUser.email)
    }

    @Test
    fun `notification preferences default values`() {
        val prefs = NotificationPreferences()

        assertTrue(prefs.travelTimeAlerts)
        assertTrue(prefs.taskReminders)
        assertTrue(prefs.eventReminders)
        assertFalse(prefs.productivityAlerts)
    }

    @Test
    fun `notification preferences copy with modified values`() {
        val originalPrefs = NotificationPreferences(
            travelTimeAlerts = true,
            taskReminders = true,
            eventReminders = true,
            productivityAlerts = false
        )

        val modifiedPrefs = originalPrefs.copy(
            taskReminders = false,
            productivityAlerts = true
        )

        assertTrue(modifiedPrefs.travelTimeAlerts)
        assertFalse(modifiedPrefs.taskReminders)
        assertTrue(modifiedPrefs.eventReminders)
        assertTrue(modifiedPrefs.productivityAlerts)
    }

    @Test
    fun `user equality check`() {
        val createdAt = System.currentTimeMillis()
        val user1 = User(
            userId = "user1",
            displayName = "Test User",
            email = "test@example.com",
            createdAt = createdAt
        )

        val user2 = User(
            userId = "user1",
            displayName = "Test User",
            email = "test@example.com",
            createdAt = createdAt
        )

        assertEquals(user1, user2)
    }
}


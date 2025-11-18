package com.varsitycollege.schedulist.data.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class TaskTest {

    @Test
    fun `task creation with default values`() {
        val task = Task()

        assertNull(task.id)
        assertEquals("", task.title)
        assertNull(task.description)
        assertNotNull(task.dueDate)
        assertEquals(EnergyLevel.LOW, task.energyLevel)
        assertFalse(task.isCompleted)
        assertEquals("", task.taskListId)
        assertEquals("", task.userId)
    }

    @Test
    fun `task creation with custom values`() {
        val dueDate = Date()
        val task = Task(
            id = "task123",
            title = "Complete project",
            description = "Finish the Android app",
            dueDate = dueDate,
            energyLevel = EnergyLevel.HIGH,
            isCompleted = true,
            taskListId = "list456",
            userId = "user789"
        )

        assertEquals("task123", task.id)
        assertEquals("Complete project", task.title)
        assertEquals("Finish the Android app", task.description)
        assertEquals(dueDate, task.dueDate)
        assertEquals(EnergyLevel.HIGH, task.energyLevel)
        assertTrue(task.isCompleted)
        assertEquals("list456", task.taskListId)
        assertEquals("user789", task.userId)
    }

    @Test
    fun `task copy with modified values`() {
        val originalTask = Task(
            id = "task1",
            title = "Original",
            isCompleted = false
        )

        val modifiedTask = originalTask.copy(
            title = "Modified",
            isCompleted = true
        )

        assertEquals("task1", modifiedTask.id)
        assertEquals("Modified", modifiedTask.title)
        assertTrue(modifiedTask.isCompleted)

        // Original should remain unchanged
        assertEquals("Original", originalTask.title)
        assertFalse(originalTask.isCompleted)
    }

    @Test
    fun `energy level enum values`() {
        val levels = EnergyLevel.values()

        assertEquals(3, levels.size)
        assertTrue(levels.contains(EnergyLevel.LOW))
        assertTrue(levels.contains(EnergyLevel.MEDIUM))
        assertTrue(levels.contains(EnergyLevel.HIGH))
    }

    @Test
    fun `task equality check`() {
        val task1 = Task(
            id = "task1",
            title = "Test Task",
            energyLevel = EnergyLevel.MEDIUM
        )

        val task2 = Task(
            id = "task1",
            title = "Test Task",
            energyLevel = EnergyLevel.MEDIUM
        )

        assertEquals(task1, task2)
    }

    @Test
    fun `task inequality check`() {
        val task1 = Task(id = "task1", title = "Task 1")
        val task2 = Task(id = "task2", title = "Task 2")

        assertNotEquals(task1, task2)
    }
}


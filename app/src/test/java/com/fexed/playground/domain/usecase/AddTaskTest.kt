package com.fexed.playground.domain.usecase

import com.fexed.playground.domain.model.Task
import com.fexed.playground.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Verifies domain title policy without Android, Room, or a real database. */
class AddTaskTest {
    /** Normalization must happen before a title crosses into the persistence boundary. */
    // JUnit discovers this method as an independently runnable test.
    @Test
    fun `trims title before saving`() = runTest {
        var savedTitle: String? = null
        val useCase = AddTask(FakeTaskRepository(onAdd = { savedTitle = it }))

        useCase("  Plan release  ")

        assertEquals("Plan release", savedTitle)
    }

    /** Blank input must fail before the repository can save anything. */
    // A separate test marker reports invalid-input behavior independently.
    @Test
    fun `rejects blank title`() = runTest {
        val useCase = AddTask(FakeTaskRepository())
        val failure = runCatching { useCase("   ") }.exceptionOrNull()

        assertEquals(IllegalArgumentException::class.java, failure?.javaClass)
    }
}

/**
 * Small test double for the domain-owned repository contract.
 *
 * Replacing this boundary keeps the tests focused on use-case behavior instead of infrastructure.
 */
private class FakeTaskRepository(
    private val onAdd: (String) -> Unit = {},
) : TaskRepository {
    override fun observeTasks(): Flow<List<Task>> = emptyFlow()

    override suspend fun addTask(title: String) = onAdd(title)

    override suspend fun setTaskCompletion(id: Long, isCompleted: Boolean) = Unit

    override suspend fun deleteTask(id: Long) = Unit
}

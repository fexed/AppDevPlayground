package com.fexed.playground.domain.repository

import com.fexed.playground.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Domain-owned contract for accessing tasks.
 *
 * This is the dependency-inversion boundary: feature logic depends on the operations it needs,
 * while data-layer implementations choose how those operations are fulfilled (Room today, another
 * source in a different app). The contract returns domain models, not storage entities.
 */
interface TaskRepository {
    /** Emits the latest task list whenever the underlying data changes. */
    fun observeTasks(): Flow<List<Task>>

    /** Persists a task with the supplied, already-validated title. */
    suspend fun addTask(title: String)

    /** Persists the requested completion state for a task. */
    suspend fun setTaskCompletion(id: Long, isCompleted: Boolean)

    /** Removes a task from the source of truth. */
    suspend fun deleteTask(id: Long)
}

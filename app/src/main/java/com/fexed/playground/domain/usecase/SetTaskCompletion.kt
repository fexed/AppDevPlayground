package com.fexed.playground.domain.usecase

import com.fexed.playground.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Represents the user action of setting a task's completion state.
 *
 * The explicit boolean makes the operation idempotent: retrying a request sets the same state
 * instead of toggling back and forth.
 */
// Enables Hilt to build this action with the app's repository binding.
class SetTaskCompletion @Inject constructor(
    private val repository: TaskRepository,
) {
    /** Delegates the requested state change to the domain repository contract. */
    suspend operator fun invoke(id: Long, isCompleted: Boolean) {
        repository.setTaskCompletion(id, isCompleted)
    }
}

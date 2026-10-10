package com.fexed.playground.domain.usecase

import com.fexed.playground.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Represents deletion as a named feature action rather than exposing storage to the UI.
 *
 * This small boundary can later host domain policies (for example, restrictions or audit rules)
 * without coupling those decisions to Compose or Room.
 */
// Enables Hilt to provide this use case wherever the feature needs it.
class DeleteTask @Inject constructor(
    private val repository: TaskRepository,
) {
    /** Removes the task with [id] through the repository contract. */
    suspend operator fun invoke(id: Long) {
        repository.deleteTask(id)
    }
}

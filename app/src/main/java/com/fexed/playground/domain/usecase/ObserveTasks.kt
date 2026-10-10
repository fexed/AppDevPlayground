package com.fexed.playground.domain.usecase

import com.fexed.playground.domain.model.Task
import com.fexed.playground.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Exposes the task observation needed by the task-list feature.
 *
 * A use case names one application action and provides a seam for future business rules. This one
 * currently delegates directly because observation itself has no additional policy.
 */
// Lets Hilt construct this use case and inject its TaskRepository dependency.
class ObserveTasks @Inject constructor(
    private val repository: TaskRepository,
) {
    /** Returns the repository's continuous stream rather than taking a one-time snapshot. */
    operator fun invoke(): Flow<List<Task>> = repository.observeTasks()
}

package com.fexed.playground.domain.usecase

import com.fexed.playground.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Applies task-title rules before creating a task.
 *
 * Keeping validation in the domain action prevents non-UI callers from bypassing the rule and
 * makes it independently testable; the repository remains responsible only for persistence.
 */
// Requests constructor injection so callers receive the configured repository implementation.
class AddTask @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Trims surrounding whitespace and rejects an empty title before delegating to the repository.
     *
     * @throws IllegalArgumentException when [title] has no non-whitespace characters.
     */
    suspend operator fun invoke(title: String) {
        val normalizedTitle = title.trim()
        require(normalizedTitle.isNotEmpty()) { "Task title cannot be blank." }
        repository.addTask(normalizedTitle)
    }
}

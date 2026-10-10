package com.fexed.playground.presentation.tasks

import com.fexed.playground.domain.model.Task

/**
 * Immutable snapshot consumed by the task-list screen.
 *
 * Keeping loading, content, and error information in one value lets Compose render a coherent
 * screen state. Replacing snapshots makes updates explicit; derived values are calculated rather
 * than stored, so they cannot drift out of sync with the task list.
 */
data class TaskListUiState(
    /** True until the first data emission arrives, or while initial observation is unresolved. */
    val isLoading: Boolean = true,
    /** Current tasks emitted by the repository's source of truth. */
    val tasks: List<Task> = emptyList(),
    /** User-presentable error detail, when an observation or action fails. */
    val errorMessage: String? = null,
) {
    /** Number of completed tasks, derived from the current list. */
    val completedCount: Int
        get() = tasks.count(Task::isCompleted)
}

package com.fexed.playground.domain.model

/**
 * Task as understood by the app's business and presentation layers.
 *
 * This model is intentionally separate from the Room entity: storage schema changes should not
 * force the UI or use cases to depend on database-specific details.
 */
data class Task(
    /** Stable identifier assigned by the local data source. */
    val id: Long,
    /** User-visible task description. */
    val title: String,
    /** Whether the task has been marked complete. */
    val isCompleted: Boolean,
)

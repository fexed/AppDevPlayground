package com.fexed.playground.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fexed.playground.domain.model.Task

/**
 * Room's persistence representation of a task.
 *
 * Keeping this database-shaped type in the data layer isolates schema concerns (such as generated
 * keys and column mapping) from the domain model. Conversion happens at the repository boundary.
 */
// Declares this data class as a Room table and supplies the SQL table name.
@Entity(tableName = "tasks")
data class TaskEntity(
    /** Zero asks Room to generate a new primary key when inserting a task. */
    // Marks the id column as the primary key and asks Room to generate it on insertion.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
) {
    /** Maps persisted data into the storage-independent model consumed by the rest of the app. */
    fun toDomain() = Task(
        id = id,
        title = title,
        isCompleted = isCompleted,
    )
}

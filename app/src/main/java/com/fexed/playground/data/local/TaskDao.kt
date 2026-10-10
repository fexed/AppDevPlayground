package com.fexed.playground.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Declarative SQL boundary for the local task table.
 *
 * Room generates the implementation and validates these queries at compile time. Returning a
 * [Flow] from the read query makes the local database observable, so writes naturally propagate
 * back to presentation state without manually synchronizing a second in-memory list.
 */
// Tells Room to generate an implementation from this interface's annotated methods.
@Dao
interface TaskDao {
    /** Emits tasks in newest-first order and re-runs when Room detects table changes. */
    // Defines SQL that Room validates at compile time and observes for table invalidation.
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    /** Inserts a task; Room assigns its generated id. */
    // Generates the INSERT statement and maps entity properties to table columns.
    @Insert
    suspend fun insert(task: TaskEntity)

    /** Sets rather than toggles completion, keeping repeated writes safe. */
    // Defines a parameterized UPDATE; Room validates the table, columns, and bound parameters.
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setCompletion(id: Long, isCompleted: Boolean)

    /** Deletes the row matching [id]. */
    // Defines a parameterized DELETE whose id argument is safely bound by Room.
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)
}

package com.fexed.playground.data.repository

import com.fexed.playground.data.local.TaskDao
import com.fexed.playground.data.local.TaskEntity
import com.fexed.playground.domain.model.Task
import com.fexed.playground.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of the domain-owned [TaskRepository] contract.
 *
 * This adapter translates between persistence and domain types and delegates I/O to the DAO.
 * The separation allows use cases to stay independent of Room and makes alternative repositories
 * straightforward to substitute in tests or a future data source.
 */
// Requests constructor injection so Hilt supplies the DAO instead of presentation creating it.
class RoomTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
) : TaskRepository {
    /** Maps every database emission into the domain representation before exposing it. */
    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeTasks().map { entities -> entities.map(TaskEntity::toDomain) }

    /** Converts the validated title into a Room entity and persists it. */
    override suspend fun addTask(title: String) {
        taskDao.insert(TaskEntity(title = title))
    }

    /** Persists an explicit completion state through the DAO. */
    override suspend fun setTaskCompletion(id: Long, isCompleted: Boolean) {
        taskDao.setCompletion(id, isCompleted)
    }

    /** Delegates deletion to the local data source. */
    override suspend fun deleteTask(id: Long) {
        taskDao.delete(id)
    }
}

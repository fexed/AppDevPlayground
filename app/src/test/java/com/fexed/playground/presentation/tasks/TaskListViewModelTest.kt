package com.fexed.playground.presentation.tasks

import androidx.lifecycle.ViewModelStore
import com.fexed.playground.domain.model.Task
import com.fexed.playground.domain.repository.TaskRepository
import com.fexed.playground.domain.usecase.AddTask
import com.fexed.playground.domain.usecase.DeleteTask
import com.fexed.playground.domain.usecase.ObserveTasks
import com.fexed.playground.domain.usecase.SetTaskCompletion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Exercises the presentation-to-domain-to-observation loop with a deterministic fake repository.
 *
 * This verifies the ViewModel contract without Compose or SQLite; database-specific behavior can
 * be covered independently by Room/instrumentation tests.
 */
// This test uses coroutine testing APIs that the library currently marks experimental.
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    // JUnit applies this rule around the test; @get targets the generated Kotlin property getter.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** A repository write should arrive in the UI through the observation stream. */
    // Marks the method for JUnit discovery and execution.
    @Test
    fun `adding task updates observed UI state`() = runTest {
        val repository = FakeTaskRepository()
        val viewModel = repository.createViewModel()
        val viewModelStore = ViewModelStore().apply {
            put("task-list", viewModel)
        }
        try {
            // Drain the initial empty-list observation before issuing the add action.
            advanceUntilIdle()

            viewModel.addTask("Draft release notes")
            advanceUntilIdle()

            assertEquals(
                listOf(Task(id = 1, title = "Draft release notes", isCompleted = false)),
                viewModel.uiState.value.tasks,
            )
            assertEquals(0, viewModel.uiState.value.completedCount)
        } finally {
            // Clearing the store cancels viewModelScope and prevents a collector leak.
            viewModelStore.clear()
        }
    }
}

/** In-memory source of truth with the same observable contract used by production. */
private class FakeTaskRepository : TaskRepository {
    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    override fun observeTasks(): Flow<List<Task>> = tasks

    override suspend fun addTask(title: String) {
        tasks.value = tasks.value + Task(nextId++, title, isCompleted = false)
    }

    override suspend fun setTaskCompletion(id: Long, isCompleted: Boolean) {
        tasks.value = tasks.value.map {
            if (it.id == id) it.copy(isCompleted = isCompleted) else it
        }
    }

    override suspend fun deleteTask(id: Long) {
        tasks.value = tasks.value.filterNot { it.id == id }
    }

    fun createViewModel() = TaskListViewModel(
        observeTasksUseCase = ObserveTasks(this),
        addTaskUseCase = AddTask(this),
        setTaskCompletionUseCase = SetTaskCompletion(this),
        deleteTaskUseCase = DeleteTask(this),
    )
}

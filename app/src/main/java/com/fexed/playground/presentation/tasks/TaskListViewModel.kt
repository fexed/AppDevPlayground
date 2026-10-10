package com.fexed.playground.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fexed.playground.domain.usecase.AddTask
import com.fexed.playground.domain.usecase.DeleteTask
import com.fexed.playground.domain.usecase.ObserveTasks
import com.fexed.playground.domain.usecase.SetTaskCompletion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Coordinates feature use cases and publishes presentation state.
 *
 * The ViewModel translates domain data and user intents into immutable UI state. It deliberately
 * does not know how tasks are persisted. Work launched in [viewModelScope] is cancelled with the
 * ViewModel lifecycle, avoiding tasks outliving the screen owner.
 */
// Enables Hilt's generated ViewModel factory to create this ViewModel with injected use cases.
@HiltViewModel
// Asks Hilt to provide constructor dependencies instead of requiring a hand-written factory.
class TaskListViewModel @Inject constructor(
    observeTasksUseCase: ObserveTasks,
    private val addTaskUseCase: AddTask,
    private val setTaskCompletionUseCase: SetTaskCompletion,
    private val deleteTaskUseCase: DeleteTask,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TaskListUiState())

    /** Exposes observable state without allowing the UI to mutate it directly. */
    val uiState = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe the source of truth continuously: successful writes are reflected by the
            // Room Flow instead of maintaining a second, potentially inconsistent task list.
            observeTasksUseCase()
                .catch { error -> showError(error) }
                .collect { tasks ->
                    mutableUiState.update {
                        it.copy(isLoading = false, tasks = tasks, errorMessage = null)
                    }
                }
        }
    }

    /** Handles the screen's add intent through the domain validation and persistence path. */
    fun addTask(title: String) {
        performAction { addTaskUseCase(title) }
    }

    /** Handles a request to set a task's completion state. */
    fun setTaskCompletion(id: Long, isCompleted: Boolean) {
        performAction { setTaskCompletionUseCase(id, isCompleted) }
    }

    /** Handles a request to delete a task. */
    fun deleteTask(id: Long) {
        performAction { deleteTaskUseCase(id) }
    }

    /** Clears an error after the screen reports that it has been dismissed. */
    fun clearError() {
        mutableUiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Runs a one-shot action in the lifecycle-bound scope and maps failures to UI state.
     *
     * Cancellation is coroutine control flow, not an app failure, so it propagates unchanged when
     * this ViewModel is cleared or the operation is otherwise cancelled.
     */
    private fun performAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showError(error)
            }
        }
    }

    /** Reduces an internal failure to the error detail needed by the presentation layer. */
    private fun showError(error: Throwable) {
        mutableUiState.update {
            it.copy(
                isLoading = false,
                errorMessage = error.message ?: "Something went wrong. Please try again.",
            )
        }
    }
}

package com.fexed.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fexed.playground.presentation.tasks.TaskListScreen
import com.fexed.playground.presentation.tasks.TaskListViewModel
import com.fexed.playground.ui.theme.PlaygroundTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Android entry point that connects the platform lifecycle to the Compose presentation.
 *
 * The activity owns no task rules or persistence logic: it collects ViewModel state while the UI
 * is active and forwards user events. This keeps rendering and business decisions in their
 * respective layers.
 */
// Makes this Android component an entry point into the Hilt graph for ViewModel injection.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TaskListViewModel by viewModels()

    /** Installs the themed screen and lifecycle-aware state collection. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                // Lifecycle-aware collection stops observing while the activity is not started,
                // while StateFlow retains the latest snapshot for the next active collection.
                val uiState = viewModel.uiState.collectAsStateWithLifecycle()
                TaskListScreen(
                    uiState = uiState.value,
                    onAddTask = viewModel::addTask,
                    onTaskCompletionChanged = viewModel::setTaskCompletion,
                    onDeleteTask = viewModel::deleteTask,
                    onErrorDismissed = viewModel::clearError,
                )
            }
        }
    }
}

package com.fexed.playground.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.fexed.playground.domain.model.Task

/**
 * Renders the task-list feature from state and reports user intents through callbacks.
 *
 * This follows unidirectional data flow: state flows down from the owner, and events flow back
 * through callbacks. The composable therefore remains independent of ViewModels and persistence,
 * making the same UI reusable in previews and tests with supplied state.
 */
// Marks this function as UI content managed by Compose's composition and recomposition runtime.
@Composable
// TopAppBar is experimental in this Material3 version, so this screen explicitly opts in.
@OptIn(ExperimentalMaterial3Api::class)
fun TaskListScreen(
    uiState: TaskListUiState,
    onAddTask: (String) -> Unit,
    onTaskCompletionChanged: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onErrorDismissed: () -> Unit,
) {
    // Text editing is ephemeral UI state; persisted tasks and business decisions live upstream.
    var title by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Task Studio")
                        Text(
                            text = "A small app, built in layers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${uiState.completedCount} of ${uiState.tasks.size} complete",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("New task") },
                    singleLine = true,
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        // Clear immediately for responsive interaction. The domain use case still
                        // validates the input so non-UI callers follow the same rule.
                        onAddTask(title)
                        title = ""
                    },
                    enabled = title.isNotBlank(),
                ) {
                    Text("Add")
                }
            }
            uiState.errorMessage?.let { message ->
                TextButton(onClick = onErrorDismissed) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                uiState.tasks.isEmpty() -> EmptyTasksMessage()

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.tasks, key = Task::id) { task ->
                        TaskRow(
                            task = task,
                            onCompletionChanged = { onTaskCompletionChanged(task.id, it) },
                            onDelete = { onDeleteTask(task.id) },
                        )
                    }
                }
            }
        }
    }
}

/** Explains the empty state and points to the primary screen action. */
// Required for a function that emits Compose UI and calls other composables.
@Composable
private fun EmptyTasksMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Nothing on the list yet", style = MaterialTheme.typography.titleMedium)
        Text(
            "Add a task above to see the full data flow in action.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Displays one task and turns its controls into parent-owned events. */
// Declares this reusable row as a composable node in the UI tree.
@Composable
private fun TaskRow(
    task: Task,
    onCompletionChanged: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCompletionChanged,
            )
            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            )
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

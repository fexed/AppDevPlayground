# Task Studio

Task Studio is a small native Android task-list app and an architecture example. It is deliberately a **single Gradle module**: the code has clear presentation, domain, and data boundaries without adding module/build complexity before the app needs it.

The demo supports adding tasks, marking them complete, deleting them, and showing a live completion count. Tasks are persisted locally and updates are observed reactively.

## Architecture at a glance

```text
User interaction
      |
      v
Compose screen (TaskListScreen)
      | callbacks                         ^ immutable UI state (StateFlow)
      v                                   |
TaskListViewModel ------------------------+
      |
      v
Use cases (AddTask, ObserveTasks, ...)
      |
      v
TaskRepository (domain contract)
      |
      v
RoomTaskRepository (data implementation)
      |
      v
TaskDao -> TaskDatabase -> SQLite
```

Dependencies point inward: the presentation layer calls domain use cases; the domain defines the repository contract; and the data layer implements that contract. The UI and use cases do not query Room directly. Hilt constructs the object graph and binds the domain repository interface to its Room-backed implementation.

### Layers and responsibilities

| Layer | Responsibility |
| --- | --- |
| **Presentation** | Draws the screen, handles user events, and exposes loading/data/error UI state. |
| **Domain** | Defines the app's `Task`, repository operations, and feature actions such as adding or observing tasks. |
| **Data** | Implements repository operations, translates Room entities into domain models, and persists data. |
| **Dependency injection** | Creates the database and DAO once, and supplies the repository and use cases where they are needed. |
| **UI theme** | Provides Material 3 colors that follow the system light/dark setting. |

The domain layer does not depend on Android UI or database APIs. It uses Kotlin `Flow` for observation and constructor injection annotations for use cases.

## How the source files fit together

Sources are under `app/src/main/java/com/fexed/playground/`.

### App entry and presentation

- `PlaygroundApplication.kt` is annotated with `@HiltAndroidApp`, which initializes Hilt's application-level dependency graph.
- `MainActivity.kt` is the launcher activity and is marked `@AndroidEntryPoint`. It obtains `TaskListViewModel`, installs the Compose content and theme, collects `uiState` with `collectAsStateWithLifecycle()`, and passes state and event callbacks to the screen.
- `presentation/tasks/TaskListScreen.kt` is the stateless screen: it renders the task input, progress/empty/list states, completion count, task rows, and errors. The input text is local Compose state; business actions are callbacks supplied by the Activity.
- `presentation/tasks/TaskListUiState.kt` is the immutable presentation snapshot (`isLoading`, `tasks`, and `errorMessage`). Its `completedCount` is derived from the current task list.
- `presentation/tasks/TaskListViewModel.kt` owns a private `MutableStateFlow` and exposes it read-only. It collects the observe use case in `viewModelScope`, translates results into UI state, and launches add/complete/delete actions. Action and stream failures are shown as an error message; coroutine cancellation is rethrown rather than treated as an app error.

### Domain

- `domain/model/Task.kt` is the app-facing task model. It does not expose Room entity details.
- `domain/repository/TaskRepository.kt` defines the operations the feature needs: observe, add, complete, and delete tasks.
- `domain/usecase/ObserveTasks.kt` exposes the task stream.
- `domain/usecase/AddTask.kt` trims a title, rejects blank input, then delegates to the repository.
- `domain/usecase/SetTaskCompletion.kt` delegates a completion change.
- `domain/usecase/DeleteTask.kt` delegates a deletion.

Use cases are intentionally small and focused. They give business rules a testable home and keep persistence decisions out of the ViewModel and Compose UI.

### Data and persistence

- `data/local/TaskEntity.kt` is the Room representation of a task. `toDomain()` maps it to the `Task` model.
- `data/local/TaskDao.kt` declares the SQL queries. Its observation query returns a `Flow`, and its write methods are `suspend` functions.
- `data/local/TaskDatabase.kt` declares the Room database and its entities.
- `data/repository/RoomTaskRepository.kt` implements `TaskRepository`, delegates reads/writes to the DAO, and maps entity lists to domain model lists.
- `app/schemas/com.fexed.playground.data.local.TaskDatabase/1.json` is the exported Room schema for version 1. Commit updated schema files alongside intentional database changes so migrations can be reviewed and tested.

Room is the source of truth for the screen's task list. After a write, the DAO's observable query emits the updated rows; the repository maps them, the ViewModel publishes new UI state, and Compose recomposes.

### Dependency injection and theme

- `di/AppModule.kt` creates the `TaskDatabase` as a singleton, provides its `TaskDao`, and binds `TaskRepository` to `RoomTaskRepository`.
- `ui/theme/Theme.kt` defines light and dark Material 3 color schemes; `MainActivity` wraps the screen in `PlaygroundTheme`.
- `app/src/main/AndroidManifest.xml` registers `PlaygroundApplication` and exposes `MainActivity` as the launcher activity.

## Example: adding a task

When the user types a title and taps **Add**, the call/data flow is:

1. `TaskListScreen` calls `onAddTask(title)`. `MainActivity` supplied that callback as `viewModel::addTask`.
2. `TaskListViewModel.addTask()` launches the operation in `viewModelScope` and calls `AddTask`.
3. `AddTask` trims the input, rejects a blank title with `require`, and calls `TaskRepository.addTask(normalizedTitle)`.
4. Hilt has supplied `RoomTaskRepository` as the `TaskRepository`. It creates a `TaskEntity` and asks `TaskDao.insert()` to save it.
5. Room writes the row to `playground.db`. The DAO's `observeTasks()` query emits the new list.
6. `RoomTaskRepository` maps each `TaskEntity` to a domain `Task`; `TaskListViewModel` collects the list and updates `TaskListUiState`.
7. `MainActivity` is collecting the state with lifecycle awareness. The changed state reaches `TaskListScreen`, which recomposes the count and task rows.

In abbreviated Kotlin, the domain-to-data boundary looks like this:

```kotlin
// Domain rule and repository call
suspend operator fun invoke(title: String) {
    val normalizedTitle = title.trim()
    require(normalizedTitle.isNotEmpty()) { "Task title cannot be blank." }
    repository.addTask(normalizedTitle)
}

// Data implementation
override suspend fun addTask(title: String) {
    taskDao.insert(TaskEntity(title = title))
}

// Reactive read path
override fun observeTasks(): Flow<List<Task>> =
    taskDao.observeTasks().map { entities -> entities.map(TaskEntity::toDomain) }
```

Completion and deletion follow the same path through `SetTaskCompletion` or `DeleteTask`. The ViewModel does not manually edit its task list after a write: the Room observation emits the persisted result, so all UI consumers see the same source of truth.

## Dependencies and build configuration

Versions are centralized in `gradle/libs.versions.toml`; the app's dependency and plugin setup is in `app/build.gradle.kts`.

| Dependency/tool | Use in this project |
| --- | --- |
| Android Gradle Plugin (AGP) | Android app configuration, packaging, and build tasks. |
| Jetpack Compose and Material 3 | Declarative UI, screen widgets, and Material styling. Compose versions are aligned by the Compose BOM. |
| Activity Compose | Hosts Compose content from the Android activity. |
| Lifecycle Runtime/ViewModel Compose | Lifecycle-aware Flow collection and ViewModel integration. |
| Room Runtime and KTX | SQLite persistence, DAO APIs, and coroutine/Flow support. |
| Room compiler via KSP | Generates Room implementation and validates DAO queries at build time; exports the schema to `app/schemas`. |
| Hilt Android | Constructor injection and application/activity/ViewModel dependency graph. |
| Hilt compiler via KSP | Generates Hilt dependency-injection code. |
| JUnit | JVM unit-test assertions and test runner. |
| Coroutines Test | Coroutine test dispatcher and `runTest` support. |
| AndroidX Test JUnit and Espresso | Instrumented Android test dependencies. |
| Compose UI Tooling | Debug-only Compose preview/inspection support. |

AGP 9 includes built-in Kotlin support, so the project does not apply the legacy Kotlin Android plugin. The Compose compiler plugin is versioned alongside Kotlin. KSP is used for Room and Hilt annotation processing.

Some build settings to know:

- `compileSdk` and `targetSdk` are 37; `minSdk` is 26.
- Java/Kotlin bytecode targets JVM 11.
- Compose is enabled in the app module.
- `android.disallowKotlinSourceSets=false` in `gradle.properties` permits KSP's generated Kotlin source sets with AGP's built-in Kotlin support.

Navigation and remote networking are intentionally not included: this example has one screen and stores its data locally. Add those integrations when the product has a second destination or a backend requirement.

## Build and test

Open the project in Android Studio, or run these commands from the project root in PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

The unit tests cover title trimming/blank-title validation and verify that adding a task through the ViewModel updates presentation state. Instrumented test dependencies are configured for future device-level tests.

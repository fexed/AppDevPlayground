package com.fexed.playground.di

import android.content.Context
import androidx.room.Room
import com.fexed.playground.data.local.TaskDao
import com.fexed.playground.data.local.TaskDatabase
import com.fexed.playground.data.repository.RoomTaskRepository
import com.fexed.playground.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the domain abstraction to the app's current data implementation.
 *
 * Consumers request [TaskRepository], not the Room adapter. This is the composition point where
 * the app selects an implementation while preserving dependency inversion between layers.
 */
// Marks this class as a Hilt module whose declarations contribute dependency bindings.
@Module
// Installs bindings in the app-lifetime component so all features share the same graph.
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /** Reuses the repository adapter for the lifetime of the application component. */
    // Maps the interface to its implementation without a manual factory method.
    @Binds
    // Scopes the implementation to the component that owns this binding.
    @Singleton
    abstract fun bindTaskRepository(repository: RoomTaskRepository): TaskRepository
}

/**
 * Builds the local persistence graph.
 *
 * The database is app-scoped because it owns the configured connection to persisted data; the DAO
 * is obtained from that shared database rather than constructed by feature code.
 */
// Marks these provider methods as Hilt dependency bindings.
@Module
// Makes database providers available from the application-wide component.
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /** Opens or creates the on-device Room database. */
    // Used because the Room builder, rather than a class constructor, creates this dependency.
    @Provides
    // Ensures one configured database instance for the lifetime of the app component.
    @Singleton
    // Selects the application Context rather than an Activity Context that could be destroyed.
    fun provideTaskDatabase(@ApplicationContext context: Context): TaskDatabase =
        Room.databaseBuilder(context, TaskDatabase::class.java, "playground.db").build()

    /** Exposes the generated DAO to repository construction. */
    // Turns the database's DAO accessor into a binding Hilt can inject.
    @Provides
    fun provideTaskDao(database: TaskDatabase): TaskDao = database.taskDao()
}

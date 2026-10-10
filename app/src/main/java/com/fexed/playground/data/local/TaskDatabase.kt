package com.fexed.playground.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database declaration for the local-first task feature.
 *
 * The version is part of the persisted schema contract. When entities change, increment it and
 * add a deliberate migration; exported schemas make previous/current database structures
 * reviewable and migration tests possible.
 */
// Registers entities and schema version so Room can generate database setup and validation code.
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TaskDatabase : RoomDatabase() {
    /** Exposes the generated DAO implementation to the data layer. */
    abstract fun taskDao(): TaskDao
}

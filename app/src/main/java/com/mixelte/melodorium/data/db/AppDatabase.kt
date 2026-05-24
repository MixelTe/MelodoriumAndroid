package com.mixelte.melodorium.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FileEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}

package com.mixelte.melodorium.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec

@Database(
    entities = [
        FileEntity::class,
        CurrentPlaylistEntity::class,
        PlaybackStateEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = AutoMigration3to4::class),
    ],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun playlistDao(): PlaylistDao
}

@DeleteColumn(tableName = "file", columnName = "id")
class AutoMigration3to4 : AutoMigrationSpec


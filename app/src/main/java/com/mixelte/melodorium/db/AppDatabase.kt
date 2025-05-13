package com.mixelte.melodorium.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [File::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}

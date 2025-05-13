package com.mixelte.melodorium.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface FileDao {
    @Query("SELECT * FROM file")
    fun getAll(): List<File>

    @Query("SELECT * FROM file WHERE rpath LIKE :rpath LIMIT 1")
    fun findByRpath(rpath: String): File

    @Insert
    fun insertAll(users: List<File>)

    @Query("DELETE FROM file")
    fun deleteAll()
}

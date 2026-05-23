package com.mixelte.melodorium.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert

@Dao
interface FileDao {
    @Query("SELECT * FROM file")
    fun getAll(): List<FileEntity>

    @Query("SELECT * FROM file WHERE rpath LIKE :rpath LIMIT 1")
    fun findByRpath(rpath: String): FileEntity

    @Insert
    fun insertAll(users: List<FileEntity>)

    @Query("DELETE FROM file")
    fun deleteAll()
}

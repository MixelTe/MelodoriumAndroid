package com.mixelte.melodorium.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FileDao {
    @Query("SELECT * FROM file")
    fun getAll(): List<FileEntity>

    @Query("SELECT * FROM file WHERE rpath LIKE :rpath LIMIT 1")
    fun findByRpath(rpath: String): FileEntity

    @Query("UPDATE file SET artworkPath = :path WHERE rpath = :rpath")
    suspend fun updateArtworkPath(rpath: String, path: String?)

    @Insert
    fun insertAll(users: List<FileEntity>)

    @Query("DELETE FROM file")
    fun deleteAll()
}

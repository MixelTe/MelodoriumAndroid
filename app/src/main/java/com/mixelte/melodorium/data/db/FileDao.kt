package com.mixelte.melodorium.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FileDao {
    @Query("SELECT * FROM file")
    suspend fun getAll(): List<FileEntity>

    @Query("SELECT * FROM file WHERE rpath = :rpath LIMIT 1")
    suspend fun findByRpath(rpath: String): FileEntity?

    @Query("UPDATE file SET artworkPath = :path WHERE rpath = :rpath")
    suspend fun updateArtworkPath(rpath: String, path: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<FileEntity>)

    @Query("DELETE FROM file")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(file: FileEntity)

    @Delete
    suspend fun deleteMultiple(files: List<FileEntity>)
}

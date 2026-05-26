package com.mixelte.melodorium.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM current_playlist ORDER BY position ASC")
    fun getCurrentPlaylistFlow(): Flow<List<CurrentPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(playlist: List<CurrentPlaylistEntity>)

    @Query("DELETE FROM current_playlist")
    suspend fun clearPlaylist()

    @Query("SELECT * FROM playback_state WHERE id = 1")
    suspend fun getPlaybackState(): PlaybackStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaybackState(state: PlaybackStateEntity)

    @Transaction
    suspend fun savePlaylist(playlist: List<CurrentPlaylistEntity>) {
        clearPlaylist()
        insertItems(playlist)
    }
}
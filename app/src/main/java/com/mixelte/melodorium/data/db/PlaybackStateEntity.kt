package com.mixelte.melodorium.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_playlist")
data class CurrentPlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rpath: String,
    val position: Int
)

@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey val id: Int = 1,
    val currentTrackRpath: String?,
    val currentPositionMs: Long,
    val isPlaying: Boolean
)
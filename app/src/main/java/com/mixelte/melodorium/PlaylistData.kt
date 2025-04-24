package com.mixelte.melodorium

import kotlinx.coroutines.flow.MutableStateFlow

object PlaylistData {
    val playlist = MutableStateFlow<MutableList<MusicFile>>(mutableListOf())
    val current = MutableStateFlow<MusicFile?>(null)

    fun addTrack(track: MusicFile) {
        playlist.value.add(track)
    }
}
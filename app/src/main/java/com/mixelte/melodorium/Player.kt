package com.mixelte.melodorium

import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import kotlinx.coroutines.flow.MutableStateFlow

object Player {
    val playlist = MutableStateFlow<MutableList<MusicFile>>(mutableListOf())
    val current = MutableStateFlow<MusicFile?>(null)
    private var mediaController: MediaController? = null

    fun addTrack(track: MusicFile) {
        playlist.value.add(track)
    }

    fun setMediaController(mediaController: MediaController) {
        this.mediaController = mediaController
    }

    fun play() {
        val playlist = playlist.value
        if (playlist.isEmpty()) return
        mediaController?.run {
            if (current.value == null) current.value = playlist[0]
            current.value?.uri?.let {
                setMediaItem(MediaItem.fromUri(it))
                play()
            }
        }
    }
}
package com.mixelte.melodorium

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.Listener
import androidx.media3.session.MediaController
import kotlinx.coroutines.flow.MutableStateFlow

object Player : Listener {
    val playlist = mutableStateListOf<MusicFile>()
    var current by mutableStateOf<MusicFile?>(null)
    var isPlaying by mutableStateOf(false)
    private var mediaController: MediaController? = null

    fun addTrack(track: MusicFile) {
        playlist.add(track)
        if (current == null) current = track
        mediaController?.run {
            addMediaItem(MediaItem.Builder().setUri(track.uri).setMediaId(track.rpath).build())
        }
    }

    fun removeTrack(track: MusicFile) {
        val i = playlist.indexOf(track)
        if (i < 0) return
        playlist.removeAt(i)
        mediaController?.run {
            removeMediaItem(i)
        }
    }

    fun setMediaController(mediaController: MediaController) {
        this.mediaController = mediaController
        mediaController.addListener(Player)
        isPlaying = mediaController.isPlaying
    }

    fun playPause() {
        mediaController?.run {
            if (isPlaying) pause()
            else play()
        }
    }

    fun prevTrack() {
        mediaController?.run {
            seekToPreviousMediaItem()
        }
    }

    fun nextTrack() {
        mediaController?.run {
            seekToNextMediaItem()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        this.isPlaying = isPlaying
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        current = mediaItem?.let { playlist.find { it.rpath == mediaItem.mediaId } }
    }
}

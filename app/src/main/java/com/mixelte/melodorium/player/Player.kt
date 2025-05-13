package com.mixelte.melodorium.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.Listener
import androidx.media3.session.MediaController
import com.mixelte.melodorium.data.MusicFile
import com.mixelte.melodorium.swap

object Player : Listener {
    val playlist = mutableStateListOf<MusicFile>()
    var current by mutableStateOf<MusicFile?>(null)
    var isPlaying by mutableStateOf(false)
    private var mediaController: MediaController? = null

    fun addTrack(track: MusicFile) {
        playlist.indexOf(track).takeIf { it >= 0 }?.let { i ->
            playlist.removeAt(i)
            mediaController?.run {
                removeMediaItem(i)
            }
        }
        playlist.add(track)
        if (current == null) current = track
        mediaController?.run {
            addMediaItem(track.toMediaItem())
        }
    }

    fun addTracks(tracks: List<MusicFile>) {
        tracks.forEach { track ->
            playlist.indexOf(track).takeIf { it >= 0 }?.let { i ->
                playlist.removeAt(i)
                mediaController?.run {
                    removeMediaItem(i)
                }
            }
            playlist.add(track)
        }
        if (current == null) current = playlist.getOrNull(0)
        mediaController?.run {
            addMediaItems(tracks.map { it.toMediaItem() })
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

    fun shuffle() {
        playlist.shuffle()
        playlist.swap(0, playlist.indexOf(current))
        mediaController?.run {
            val curPos = currentPosition
            setMediaItems(playlist.map { it.toMediaItem() })
            seekTo(0, curPos)
        }
    }

    fun shuffle(selected: List<MusicFile>) {
        var cur = playlist.indexOf(current)
        val items = selected.map { it to playlist.indexOf(it) }.filter { it.second >= 0 }
        items.shuffled().forEachIndexed { i, it ->
            playlist[it.second] = items[i].first
            if (items[i].first == current) cur = it.second
        }
        mediaController?.run {
            val curPos = currentPosition
            setMediaItems(playlist.map { it.toMediaItem() })
            if (cur >= 0)
                seekTo(cur, curPos)
        }
    }

    fun setMediaController(mediaController: MediaController) {
        this.mediaController = mediaController
        mediaController.addListener(Player)
        isPlaying = mediaController.isPlaying
    }

    fun play(item: MusicFile) {
        val i = playlist.indexOf(item)
        if (i < 0) return
        mediaController?.run {
            seekTo(i, 0)
        }
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

private fun MusicFile.toMediaItem(): MediaItem {
    return MediaItem.Builder().setUri(this.uri).setMediaId(this.rpath).build()
}

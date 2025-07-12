package com.mixelte.melodorium.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.Listener
import androidx.media3.session.MediaController
import com.mixelte.melodorium.data.MusicDataFilter
import com.mixelte.melodorium.data.MusicFile
import com.mixelte.melodorium.swap

object Player : Listener {
    val playlist = mutableStateListOf<PlayerItem>()
    var current by mutableStateOf<PlayerItem?>(null)
    var isPlaying by mutableStateOf(false)
    var autoAdd by mutableStateOf(true)
    private var mediaController: MediaController? = null

    fun addTrack(track: MusicFile) {
        val item = PlayerItem(track)
        playlist.add(item)
        if (current == null) current = item
        mediaController?.run {
            addMediaItem(item.mediaItem)
        }
    }

    fun addTracks(tracks: List<MusicFile>) {
        val items = tracks.map { PlayerItem(it) }
        playlist.addAll(items)
        if (current == null) current = playlist.getOrNull(0)
        mediaController?.run {
            addMediaItems(items.map { it.mediaItem })
        }
    }

    fun removeTrack(track: PlayerItem) {
        val i = playlist.indexOf(track)
        if (i < 0) return
        playlist.removeAt(i)
        mediaController?.run {
            removeMediaItem(i)
        }
    }

    fun clear() {
        mediaController?.run {
            playlist.clear()
            clearMediaItems()
        }
    }

    fun shuffle() {
        playlist.shuffle()
        playlist.swap(0, playlist.indexOf(current))
        mediaController?.run {
            val curPos = currentPosition
            setMediaItems(playlist.map { it.mediaItem })
            seekTo(0, curPos)
        }
    }

    fun shuffle(selected: List<PlayerItem>) {
        var cur = playlist.indexOf(current)
        val items = selected.map { it to playlist.indexOf(it) }.filter { it.second >= 0 }
        items.shuffled().forEachIndexed { i, it ->
            playlist[it.second] = items[i].first
            if (items[i].first == current) cur = it.second
        }
        mediaController?.run {
            val curPos = currentPosition
            setMediaItems(playlist.map { it.mediaItem })
            if (cur >= 0)
                seekTo(cur, curPos)
        }
    }

    fun setMediaController(mediaController: MediaController) {
        this.mediaController = mediaController
        mediaController.addListener(Player)
        isPlaying = mediaController.isPlaying
    }

    fun play(item: PlayerItem) {
        val i = playlist.indexOf(item)
        if (i < 0) return
        mediaController?.run {
            seekTo(i, 0)
        }
    }

    fun playPause() {
        mediaController?.run {
            if (isPlaying) pause()
            else {
                if (autoAdd && playlist.isEmpty())
                    addNextTrack()
                play()
            }
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

    fun addNextTrack() {
        val tracks = MusicDataFilter.files.map { it to playlist.count { item -> it == item.file } }
        val min = tracks.minBy { it.second }.second
        val next = tracks.filter { it.second == min }.random().first
        addTrack(next)
    }

    fun moveTrack(item: PlayerItem, toI: Int) =
        moveTrack(playlist.indexOf(item), toI)

    fun moveTrack(i: Int, toI: Int) {
        if (i < 0 || i >= playlist.size ||
            toI < 0 || toI >= playlist.size
        )
            return
        val item = playlist[i]
        playlist.removeAt(i)
        playlist.add(toI, item)
        mediaController?.run {
            moveMediaItem(i, toI)
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        this.isPlaying = isPlaying
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        current = mediaItem?.let { playlist.find { it.mediaId == mediaItem.mediaId } }
        current?.let {
            if (autoAdd && playlist.last() == it)
                addNextTrack()
        }
    }
}

class PlayerItem(val file: MusicFile) {
    val mediaId = (lastMediaId++).toString()
    val mediaItem = MediaItem.Builder().setUri(file.uri).setMediaId(this.mediaId).build()

    companion object {
        var lastMediaId = 1
    }
}
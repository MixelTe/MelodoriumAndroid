package com.mixelte.melodorium.ui.playlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.domain.MusicFilterManager
import com.mixelte.melodorium.player.PlaybackManager
import com.mixelte.melodorium.player.PlayerItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playbackManager: PlaybackManager,
    private val filterManager: MusicFilterManager
) : ViewModel() {
    val playlist = playbackManager.playlist
    val currentTrack = playbackManager.currentItem
    val isPlaying = playbackManager.isPlaying

    var autoAddNext by mutableStateOf(true)

    init {
        playbackManager.onTrackEndedListener = {
            if (autoAddNext) {
                addNextSmartTrack()
            }
        }
    }

    fun playPause() {
        if (!isPlaying.value && autoAddNext && playlist.value.isEmpty()) {
            addNextSmartTrack()
        }
        playbackManager.playPause()
    }

    fun addNextSmartTrack() {
        viewModelScope.launch {
            val allFiles = filterManager.filteredFiles.first()
            if (allFiles.isEmpty()) return@launch

            val tracksWithCount = allFiles.map { file ->
                file to playlist.value.count { it.file == file }
            }

            val minCount = tracksWithCount.minByOrNull { it.second }?.second ?: 0
            val candidates = tracksWithCount.filter { it.second == minCount }

            if (candidates.isNotEmpty()) {
                val nextTrack = candidates.random().first
                playbackManager.addTrack(nextTrack)
            }
        }
    }

    fun removeTrack(item: PlayerItem) {
        val index = playlist.value.indexOf(item)
        playbackManager.removeTrack(index)
    }

    fun removeTracks(items: List<PlayerItem>) {
        val indices = items.map { playlist.value.indexOf(it) }.filter { it >= 0 }
        playbackManager.removeTracks(indices)
    }

    fun shufflePlaylist(selectedItems: List<PlayerItem> = emptyList()) {
        if (selectedItems.isEmpty()) {
            playbackManager.shuffle(null)
        } else {
            val indices = selectedItems.map { playlist.value.indexOf(it) }.filter { it >= 0 }
            playbackManager.shuffle(indices)
        }
    }

    fun moveTrackUp(item: PlayerItem) {
        val i = playlist.value.indexOf(item)
        if (i > 0) playbackManager.moveTrack(i, i - 1)
    }

    fun moveTrackDown(item: PlayerItem) {
        val i = playlist.value.indexOf(item)
        if (i >= 0 && i < playlist.value.size - 1) playbackManager.moveTrack(i, i + 1)
    }

    fun moveTrackToEnd(item: PlayerItem) {
        val i = playlist.value.indexOf(item)
        if (i >= 0) playbackManager.moveTrack(i, playlist.value.size - 1)
    }

    fun playTrack(item: PlayerItem) {
        val i = playlist.value.indexOf(item)
        if (i >= 0) playbackManager.seekTo(i)
    }

    fun clearPlaylist() = playbackManager.clear()
    fun nextTrack() = playbackManager.next()
    fun prevTrack() = playbackManager.prev()
}
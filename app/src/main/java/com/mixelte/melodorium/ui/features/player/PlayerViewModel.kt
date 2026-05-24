package com.mixelte.melodorium.ui.features.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.domain.MusicFilterManager
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.player.PlaybackManager
import com.mixelte.melodorium.player.PlayerItem
import com.mixelte.melodorium.toTimeString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val trackName: String? = null,
    val artistName: String? = null,
    val trackMood: MusicMood? = null,
    val trackLike: MusicLike? = null,
    val trackLang: MusicLang? = null,
    val trackArtwork: Uri? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentTime: String? = null,
    val fullTime: String? = null,
)

class PlayerViewModel(
    private val playbackManager: PlaybackManager,
    private val filterManager: MusicFilterManager
) : ViewModel() {
    private val playlist = playbackManager.playlist
    private val currentTrack = playbackManager.currentItem
    private val currentPosition = playbackManager.currentPosition
    private val duration = playbackManager.duration
    private val isPlaying = playbackManager.isPlaying

    private val _autoAddNext = MutableStateFlow(true)
    val autoAddNext = _autoAddNext.asStateFlow()

    val playerState =
        combine(currentTrack, isPlaying, currentPosition, duration) { track, isPlaying, currentPosition, duration ->
            val progress = if (duration > 0) currentPosition.toFloat() / duration else 0F
            PlayerUiState(
                trackName = track?.file?.name,
                artistName = track?.file?.author,
                trackMood = track?.file?.mood,
                trackLike = track?.file?.like,
                trackLang = track?.file?.lang,
                trackArtwork = track?.file?.artworkUri,
                isPlaying = isPlaying,
                progress = progress,
                currentTime = currentPosition.toTimeString(),
                fullTime = duration.toTimeString(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    init {
        playbackManager.onTrackEndedListener = {
            if (_autoAddNext.value) {
                addNextSmartTrack()
            }
        }
        viewModelScope.launch {
            combine(filterManager.isLoading, playbackManager.playlist) { isLoading, playlist ->
                !isLoading && playlist.isEmpty()
            }.collect {
                if (it) {
                    addNextSmartTrack()
                }
            }
        }
    }

    fun togglePlayPause() {
        if (!isPlaying.value && _autoAddNext.value && playlist.value.isEmpty()) {
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
    fun seekTo(positionPercent: Float) = playbackManager.seekTo((positionPercent * duration.value).toLong())
}
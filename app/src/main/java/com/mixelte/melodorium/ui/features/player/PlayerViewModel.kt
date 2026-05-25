package com.mixelte.melodorium.ui.features.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.domain.MusicFilterManager
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.player.PlaybackManager
import com.mixelte.melodorium.toTimeString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class PlayerUiTrack(
    val id: String,
    val title: String,
    val artist: String,
    val mood: MusicMood,
    val like: MusicLike,
    val lang: MusicLang,
    val artwork: File? = null,
    val isPlaying: Boolean = false,
)

data class PlayerUiState(
    val track: PlayerUiTrack? = null,
    val progress: Float = 0f,
    val currentTime: String? = null,
    val fullTime: String? = null,
)

class PlayerViewModel(
    private val playbackManager: PlaybackManager,
    private val filterManager: MusicFilterManager,
    private val musicRepository: MusicRepository,
) : ViewModel() {
    private val playlist = playbackManager.playlist
    private val currentTrack = playbackManager.currentItem
    private val currentPosition = playbackManager.currentPosition
    private val duration = playbackManager.duration
    private val isPlaying = playbackManager.isPlaying

    private val _currentArtwork = MutableStateFlow<File?>(null)
    val currentArtwork: StateFlow<File?> = _currentArtwork.asStateFlow()

    val playerState =
        combine(
            currentTrack,
            isPlaying,
            currentPosition,
            duration,
            currentArtwork
        ) { track, isPlaying, currentPosition, duration, trackArtwork ->
            val progress = if (duration > 0) currentPosition.toFloat() / duration else 0F
            PlayerUiState(
                track = track?.let {
                    val f = it.file
                    PlayerUiTrack(
                        f.rpath,
                        f.name,
                        f.author,
                        f.mood,
                        f.like,
                        f.lang,
                        trackArtwork,
                        isPlaying
                    )
                },
                progress = progress,
                currentTime = currentPosition.toTimeString(),
                fullTime = duration.toTimeString(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    val tracks = combine(playlist, currentTrack, currentArtwork) { items, currentTrack, _ ->
        items.map {
            val file = it.file
            PlayerUiTrack(
                file.rpath,
                file.name,
                file.author,
                file.mood,
                file.like,
                file.lang,
                file.artworkFile,
                currentTrack?.file?.rpath == file.rpath,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        playbackManager.onTrackChangedListener = {
            if (playbackManager.playlist.value.lastOrNull() == playbackManager.currentItem.value) {
                addNextSmartTrack()
            }
            viewModelScope.launch {
                _currentArtwork.value = playbackManager.currentItem.value?.file?.let {
                    musicRepository.getArtworkFile(it)
                }
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
        if (!isPlaying.value && playlist.value.isEmpty()) {
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

    fun removeTrack(item: PlayerUiTrack) {
        val index = playlist.value.indexOfFirst { it.file.rpath == item.id }
        playbackManager.removeTrack(index)
    }

    fun removeTracks(items: List<PlayerUiTrack>) {
        val indices = items.map { item -> playlist.value.indexOfFirst { it.file.rpath == item.id } }
            .filter { it >= 0 }
        playbackManager.removeTracks(indices)
    }

    fun shufflePlaylist(selectedItems: List<PlayerUiTrack> = emptyList()) {
        if (selectedItems.isEmpty()) {
            playbackManager.shuffle(null)
        } else {
            val indices = selectedItems.map { item -> playlist.value.indexOfFirst { it.file.rpath == item.id } }
                .filter { it >= 0 }
            playbackManager.shuffle(indices)
        }
    }

    fun moveTrackUp(item: PlayerUiTrack) {
        val i = playlist.value.indexOfFirst { it.file.rpath == item.id }
        if (i > 0) playbackManager.moveTrack(i, i - 1)
    }

    fun moveTrackDown(item: PlayerUiTrack) {
        val i = playlist.value.indexOfFirst { it.file.rpath == item.id }
        if (i >= 0 && i < playlist.value.size - 1) playbackManager.moveTrack(i, i + 1)
    }

    fun moveTrackToEnd(item: PlayerUiTrack) {
        val i = playlist.value.indexOfFirst { it.file.rpath == item.id }
        if (i >= 0) playbackManager.moveTrack(i, playlist.value.size - 1)
    }

    fun playTrack(item: PlayerUiTrack) {
        val i = playlist.value.indexOfFirst { it.file.rpath == item.id }
        if (i >= 0) playbackManager.seekTo(i)
    }

    fun clearPlaylist() = playbackManager.clear()
    fun nextTrack() = playbackManager.next()
    fun prevTrack() = playbackManager.prev()
    fun seekTo(positionPercent: Float) = playbackManager.seekTo((positionPercent * duration.value).toLong())
}
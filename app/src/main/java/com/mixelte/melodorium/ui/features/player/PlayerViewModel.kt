package com.mixelte.melodorium.ui.features.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.domain.MusicFilterManager
import com.mixelte.melodorium.player.PlaybackManager
import com.mixelte.melodorium.toTimeString
import com.mixelte.melodorium.ui.common.UiTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val track: UiTrack? = null,
    val progress: Float = 0f,
    val currentTime: String? = null,
    val fullTime: String? = null,
)

class PlayerViewModel(
    private val playbackManager: PlaybackManager,
    private val filterManager: MusicFilterManager,
) : ViewModel() {
    private val playlist = playbackManager.playlist
    private val currentTrack = playbackManager.currentItem
    private val currentArtwork = playbackManager.currentItemArtwork
    private val currentPosition = playbackManager.currentPosition
    private val duration = playbackManager.duration
    private val isPlaying = playbackManager.isPlaying

    val isWaveMode = MutableStateFlow(true)

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
                    UiTrack(
                        it.id,
                        it.file.name,
                        it.file.author,
                        it.file.mood,
                        it.file.like,
                        it.file.lang,
                        it.file.emo,
                        it.file.publicEnum,
                        it.file.artworkFile ?: trackArtwork,
                        isPlaying
                    )
                },
                progress = progress,
                currentTime = currentPosition.toTimeString(),
                fullTime = duration.toTimeString(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    val tracks = combine(playlist, currentTrack, currentArtwork) { items, currentTrack, _ ->
        items?.map {
            UiTrack(
                it.id,
                it.file.name,
                it.file.author,
                it.file.mood,
                it.file.like,
                it.file.lang,
                it.file.emo,
                it.file.publicEnum,
                it.file.artworkFile,
                currentTrack?.id == it.id,
            )
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            playbackManager.currentItem.collect { currentItem ->
                val playlist = playbackManager.playlist.value
                if (playlist != null && playlist.lastOrNull()?.id == currentItem?.id) {
                    addNextSmartTrack()
                }
            }
        }
        viewModelScope.launch {
            combine(filterManager.isLoading, playbackManager.playlist) { isLoading, playlist ->
                !isLoading && playlist != null && playlist.isEmpty()
            }.collect { shouldAdd ->
                if (shouldAdd) {
                    addNextSmartTrack()
                }
            }
        }
    }

    fun togglePlayPause() {
        if (!isPlaying.value && playlist.value?.isEmpty() == true) {
            addNextSmartTrack()
        }
        playbackManager.playPause()
    }

    fun addNextSmartTrack() {
        if (!isWaveMode.value) return
        viewModelScope.launch {
            val allFiles = filterManager.filteredFiles.first()
            if (allFiles.isEmpty()) return@launch

            val tracksWithCount = allFiles.map { file ->
                file to (playlist.value?.count { it.file.rpath == file.rpath } ?: 0)
            }

            val minCount = tracksWithCount.minByOrNull { it.second }?.second ?: 0
            val candidates = tracksWithCount.filter { it.second == minCount }.map { it.first }

            val nextTrack = candidates.ifEmpty { allFiles }.random()
            playbackManager.addTrack(nextTrack)
        }
    }

    fun removeTrack(item: UiTrack) = playbackManager.removeTrack(item.id)
    fun removeTracks(items: List<UiTrack>) = playbackManager.removeTracks(items.map { it.id })

    fun shufflePlaylist(selectedItems: List<UiTrack>? = null) {
        playbackManager.shuffle(selectedItems?.let { items -> items.map { it.id } })
    }

    fun moveTrackUp(item: UiTrack) {
        val i = playbackManager.indexOfEntry(item.id)
        playbackManager.moveTrack(i, i - 1)
    }

    fun moveTrackDown(item: UiTrack) {
        val i = playbackManager.indexOfEntry(item.id)
        playbackManager.moveTrack(i, i + 1)
    }

    fun moveTrackToEnd(item: UiTrack) {
        val i = playbackManager.indexOfEntry(item.id)
        playlist.value?.let { playbackManager.moveTrack(i, it.size - 1) }
    }

    fun playTrack(item: UiTrack) {
        val i = playbackManager.indexOfEntry(item.id)
        if (i >= 0) playbackManager.seekTo(i)
    }

    fun clearPlaylist() = playbackManager.clear()
    fun nextTrack() = playbackManager.next()
    fun prevTrack() = playbackManager.prev()
    fun seekTo(positionPercent: Float) = playbackManager.seekTo((positionPercent * duration.value).toLong())
}
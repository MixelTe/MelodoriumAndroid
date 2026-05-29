package com.mixelte.melodorium.ui.features.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.cyrillicToLatin
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.player.PlaybackManager
import com.mixelte.melodorium.toLongHash
import com.mixelte.melodorium.ui.common.UiTrack
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val playbackManager: PlaybackManager,
) : ViewModel() {
    private val _eventChannel = Channel<String>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val tracks = combine(musicRepository.files, query) { files, query ->
        val query = query.trim().lowercase().cyrillicToLatin()
        files?.filter {
            query.isEmpty() || it.nameNorm.contains(query) || it.authorNorm.contains(query)
        }?.map {
            UiTrack(
                id = it.rpath.toLongHash(),
                rpath = it.rpath,
                title = it.name,
                artist = it.author,
                mood = it.mood,
                like = it.like,
                lang = it.lang,
                emo = it.emo,
                public = it.publicEnum,
                artwork = it.artworkFile,
                isPlaying = false,
            )
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(query: String) {
        _query.value = query
        _isSearching.value = true
    }

    fun cancelSearching() {
        _isSearching.value = false
    }

    fun addTrackToQueue(track: UiTrack) {
        viewModelScope.launch {
            val musicFile = musicRepository.files.value?.find {
                it.rpath.toLongHash() == track.id
            }

            if (musicFile != null) {
                playbackManager.addTrack(musicFile)
                _eventChannel.send("Трек добавлен в очередь: ${track.title}")
            } else {
                _eventChannel.send("Не удалось добавить трек: возможно, он был отфильтрован")
            }
        }
    }
}

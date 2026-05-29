package com.mixelte.melodorium.ui.features.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.cyrillicToLatin
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.toLongHash
import com.mixelte.melodorium.ui.common.UiTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel(
    private val musicRepository: MusicRepository
) : ViewModel() {
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val tracks = combine(musicRepository.files, query) { files, query ->
        val query = query.trim().lowercase().cyrillicToLatin()
        files.filter {
            query.isEmpty() || it.nameNorm.contains(query) || it.authorNorm.contains(query)
        }.map {
            UiTrack(
                id = it.rpath.toLongHash(),
                rpath = it.rpath,
                title = it.name,
                artist = it.author,
                mood = it.mood,
                like = it.like,
                lang = it.lang,
                artwork = it.artworkFile,
                isPlaying = false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(query: String) {
        _query.value = query
        _isSearching.value = true
    }
    fun cancelSearching() {
        _isSearching.value = false
    }
}

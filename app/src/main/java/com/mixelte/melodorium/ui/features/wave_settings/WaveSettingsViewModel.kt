package com.mixelte.melodorium.ui.features.wave_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.cyrillicToLatin
import com.mixelte.melodorium.domain.FilterState
import com.mixelte.melodorium.domain.MusicFilterManager
import com.mixelte.melodorium.domain.models.MusicEmo
import com.mixelte.melodorium.domain.models.MusicFile
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.domain.models.MusicPublic
import com.mixelte.melodorium.player.PlaybackManager
import com.mixelte.melodorium.toEnumOrNull
import com.mixelte.melodorium.toLongHash
import com.mixelte.melodorium.ui.common.UiTrack
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WaveSettingsUiChip(
    val id: String,
    val text: String,
    val isSelected: Boolean,
)

data class ArtistSearchState(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
)

data class WaveSettingsUiState(
    val artists: List<WaveSettingsUiChip> = emptyList(),
    val moods: List<WaveSettingsUiChip> = emptyList(),
    val likes: List<WaveSettingsUiChip> = emptyList(),
    val langs: List<WaveSettingsUiChip> = emptyList(),
    val tags: List<WaveSettingsUiChip> = emptyList(),
    val other: List<WaveSettingsUiChip> = emptyList(),
    val trackCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class WaveSettingsViewModel(
    private val filterManager: MusicFilterManager,
    private val playbackManager: PlaybackManager,
) : ViewModel() {
    private val _eventChannel = Channel<String>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

    val filteredTracks = filterManager.filteredFiles.map { files ->
        files.map {
            UiTrack(
                it.rpath.toLongHash(),
                it.name,
                it.author,
                it.mood,
                it.like,
                it.lang,
                it.emo,
                it.publicEnum,
                it.artworkFile,
                false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState =
        combine(
            filterManager.state,
            filterManager.filteredFiles,
            filterManager.tags,
            filterManager.authors,
            filterManager.error,
            filterManager.isLoading
        ) { args: Array<Any?> ->
            val state = args[0] as FilterState
            val files = args[1] as List<MusicFile>
            val tags = args[2] as List<String>
            val authors = args[3] as List<String>
            val error = args[4] as String?
            val isLoading = args[5] as Boolean

            WaveSettingsUiState(
                trackCount = files.size,
                error = error,
                isLoading = isLoading,
                artists = authors.map {
                    WaveSettingsUiChip(
                        it,
                        it,
                        it in state.selectedAuthors
                    )
                },
                moods = MusicMood.entries.map {
                    WaveSettingsUiChip(
                        it.toString(),
                        it.toName(),
                        it in state.selectedMoods
                    )
                },
                likes = MusicLike.entries.map {
                    WaveSettingsUiChip(
                        it.toString(),
                        it.toName(),
                        it in state.selectedLikes
                    )
                },
                langs = MusicLang.entries.map {
                    WaveSettingsUiChip(
                        it.toString(),
                        it.toName(),
                        it in state.selectedLangs
                    )
                },
                tags = tags.map {
                    WaveSettingsUiChip(
                        it,
                        if (it == "") "⚪" else it,
                        it in state.selectedTags
                    )
                },
                other = MusicEmo.entries.map {
                    WaveSettingsUiChip(
                        it.toString(),
                        it.toName(),
                        it in state.selectedEmos
                    )
                } + MusicPublic.entries.map {
                    WaveSettingsUiChip(
                        it.toString(),
                        it.toName(),
                        it in state.selectedPublics
                    )
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaveSettingsUiState())

    private val _artistSearchQuery = MutableStateFlow("")
    val artistSearchState =
        combine(_artistSearchQuery, filterManager.authors, filterManager.state) { userQuery, authors, filters ->
            val query = userQuery.trim().lowercase().cyrillicToLatin()
            ArtistSearchState(
                query = userQuery,
                suggestions = authors.filter { it.lowercase().contains(query) && it !in filters.selectedAuthors },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArtistSearchState())

    fun onArtistToggled(artist: String, isSelected: Boolean? = null) = filterManager.toggleAuthor(artist, isSelected)
    fun onMoodToggled(mood: String, isSelected: Boolean? = null) =
        filterManager.toggleMood(MusicMood.valueOf(mood), isSelected)

    fun onLikeToggled(like: String, isSelected: Boolean? = null) =
        filterManager.toggleLike(MusicLike.valueOf(like), isSelected)

    fun onLangToggled(lang: String, isSelected: Boolean? = null) =
        filterManager.toggleLang(MusicLang.valueOf(lang), isSelected)

    fun onTagToggled(tag: String, isSelected: Boolean? = null) = filterManager.toggleTag(tag, isSelected)

    fun onOtherToggled(item: String, isSelected: Boolean? = null) {
        item.toEnumOrNull<MusicEmo>()?.let {
            filterManager.toggleEmo(it, isSelected)
        }
        item.toEnumOrNull<MusicPublic>()?.let {
            filterManager.togglePublic(it, isSelected)
        }
    }

    fun clearFilters() = filterManager.reset()

    fun onArtistSearchQueryChanged(newQuery: String) {
        _artistSearchQuery.value = newQuery
    }

    fun addTrackToQueue(track: UiTrack) {
        viewModelScope.launch {
            val musicFile = filterManager.filteredFiles.first().find {
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

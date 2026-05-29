package com.mixelte.melodorium.ui.features.wave_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.domain.FilterState
import com.mixelte.melodorium.domain.MusicFilterManager
import com.mixelte.melodorium.domain.models.MusicFile
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.toLongHash
import com.mixelte.melodorium.ui.common.UiTrack
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WaveSettingsUiChip(
    val id: String,
    val text: String,
    val isSelected: Boolean,
)

data class WaveSettingsUiState(
    val authors: List<WaveSettingsUiChip> = emptyList(),
    val moods: List<WaveSettingsUiChip> = emptyList(),
    val likes: List<WaveSettingsUiChip> = emptyList(),
    val langs: List<WaveSettingsUiChip> = emptyList(),
//    val emos: List<WaveSettingsUiChip> = emptyList(),
    val tags: List<WaveSettingsUiChip> = emptyList(),
//    val selectedPublics: List<WaveSettingsUiChip> = emptyList()
    val trackCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class WaveSettingsViewModel(
    private val filterManager: MusicFilterManager,
) : ViewModel() {

    val filteredTracks = filterManager.filteredFiles.map { files ->
        files.map {
            UiTrack(
                it.rpath.toLongHash(),
                it.name,
                it.author,
                it.mood,
                it.like,
                it.lang,
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
                authors = authors.map {
                    WaveSettingsUiChip(
                        it.lowercase(),
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
                        it,
                        it in state.selectedTags
                    )
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaveSettingsUiState())

    fun onAuthorToggled(author: String, isSelected: Boolean? = null) = filterManager.toggleAuthor(author, isSelected)
    fun onMoodToggled(mood: String, isSelected: Boolean? = null) =
        filterManager.toggleMood(MusicMood.valueOf(mood), isSelected)

    fun onLikeToggled(like: String, isSelected: Boolean? = null) =
        filterManager.toggleLike(MusicLike.valueOf(like), isSelected)

    fun onLangToggled(lang: String, isSelected: Boolean? = null) =
        filterManager.toggleLang(MusicLang.valueOf(lang), isSelected)

    //    fun onEmoToggled(emo: String, isSelected: Boolean? = null) = filterManager.toggleEmo(MusicEmo.valueOf(emo), isSelected)
    fun onTagToggled(tag: String, isSelected: Boolean? = null) = filterManager.toggleTag(tag, isSelected)

    fun clearFilters() = filterManager.reset()
}

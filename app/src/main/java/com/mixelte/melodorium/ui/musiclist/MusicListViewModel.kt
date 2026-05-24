package com.mixelte.melodorium.ui.musiclist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.data.repository.SettingsRepository
import com.mixelte.melodorium.domain.MusicFilterManager
import com.mixelte.melodorium.domain.models.MusicEmo
import com.mixelte.melodorium.domain.models.MusicFile
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.domain.models.MusicPublic
import com.mixelte.melodorium.player.PlaybackManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MusicListViewModel(
    private val settingsRepository: SettingsRepository,
    private val musicRepository: MusicRepository,
    private val filterManager: MusicFilterManager,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    val folders = musicRepository.folders
    val tags = musicRepository.tags
    val isLoading = musicRepository.isLoading
    val error = musicRepository.error

    val filters = filterManager.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = filterManager.state.value
    )

    val files = filterManager.filteredFiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val title = filterManager.title.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    fun onAuthorQueryChanged(query: String) = filterManager.updateAuthorQuery(query)
    fun onNameQueryChanged(query: String) = filterManager.updateNameQuery(query)
    fun onMoodToggled(mood: MusicMood, isSelected: Boolean? = null) = filterManager.toggleMood(mood, isSelected)
    fun onLikeToggled(like: MusicLike, isSelected: Boolean? = null) = filterManager.toggleLike(like, isSelected)
    fun onLangToggled(lang: MusicLang, isSelected: Boolean? = null) = filterManager.toggleLang(lang, isSelected)
    fun onEmoToggled(emo: MusicEmo, isSelected: Boolean? = null) = filterManager.toggleEmo(emo, isSelected)
    fun onTagToggled(tag: String, isSelected: Boolean? = null) = filterManager.toggleTag(tag, isSelected)
    fun onFolderToggled(folder: String, isSelected: Boolean? = null) = filterManager.toggleFolder(folder, isSelected)
    fun onPublicToggled(public: MusicPublic, isSelected: Boolean? = null) =
        filterManager.togglePublic(public, isSelected)

    fun onResetFilters() = filterManager.reset()

    fun updateFiles() {
        viewModelScope.launch {
            val file = settingsRepository.musicDatafile.firstOrNull()
            val root = settingsRepository.musicRootFolder.firstOrNull()

            if (file != null && root != null) {
                musicRepository.loadMusicData(datafileUri = file, rootFolderUri = root, clearCache = true)
            }
        }
    }

    fun addTrackToPlaylist(track: MusicFile) {
        playbackManager.addTrack(track)
    }

    fun addTracksToPlaylist(tracks: List<MusicFile>) {
        playbackManager.addTracks(tracks)
    }

    init {
        viewModelScope.launch {
            combine(settingsRepository.musicDatafile, settingsRepository.musicRootFolder) { file, root ->
                if (file != null && root != null) Pair(file, root) else null
            }.collect { pair ->
                pair?.let { (file, root) ->
                    musicRepository.loadMusicData(datafileUri = file, rootFolderUri = root)
                }
            }
        }
    }
}
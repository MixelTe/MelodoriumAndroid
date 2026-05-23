package com.mixelte.melodorium.ui.musiclist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.data.repository.SettingsRepository
import com.mixelte.melodorium.models.MusicEmo
import com.mixelte.melodorium.models.MusicFile
import com.mixelte.melodorium.models.MusicLang
import com.mixelte.melodorium.models.MusicLike
import com.mixelte.melodorium.models.MusicMood
import com.mixelte.melodorium.models.MusicPublic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class FilterState(
    val author: String,
    val name: String,
    val moods: List<MusicMood>,
    val likes: List<MusicLike>,
    val langs: List<MusicLang>,
    val emos: List<MusicEmo>,
    val tags: List<String>,
    val folders: List<String>,
    val publics: List<MusicPublic>
)

class MusicListViewModel(
    private val settingsRepository: SettingsRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {
    var authorFilter by mutableStateOf("")
    var nameFilter by mutableStateOf("")
    val selectedMoods = MusicMood.entries.toMutableStateList()
    var selectedLikes = MusicLike.entries.toMutableStateList()
    var selectedLangs = MusicLang.entries.toMutableStateList()
    var selectedEmos = MusicEmo.entries.toMutableStateList()
    var selectedTags = mutableStateListOf<String>()
    var selectedFolders = mutableStateListOf<String>()
    var selectedPublics = mutableStateListOf<MusicPublic>()

    val folders = musicRepository.folders
    val tags = musicRepository.tags
    val isLoading = musicRepository.isLoading
    val error = musicRepository.error

    private val filterStateFlow = snapshotFlow {
        FilterState(
            author = authorFilter,
            name = nameFilter,
            moods = selectedMoods.toList(),
            likes = selectedLikes.toList(),
            langs = selectedLangs.toList(),
            emos = selectedEmos.toList(),
            tags = selectedTags.toList(),
            folders = selectedFolders.toList(),
            publics = selectedPublics.toList()
        )
    }

    val filteredFiles: StateFlow<List<MusicFile>> = combine(
        musicRepository.files,
        filterStateFlow
    ) { files, filters ->
        if (files.isEmpty()) return@combine emptyList()
        val authorLower = filters.author.lowercase()
        val nameLower = filters.name.lowercase()

        files.filter { file ->
            (authorLower.isEmpty() || authorLower in file.authorNorm) &&
                    (nameLower.isEmpty() || nameLower in file.nameNorm) &&
                    (filters.moods.isEmpty() || file.mood in filters.moods) &&
                    (filters.likes.isEmpty() || file.like in filters.likes) &&
                    (filters.langs.isEmpty() || file.lang in filters.langs) &&
                    (filters.emos.isEmpty() || file.emo in filters.emos) &&
                    (filters.tags.isEmpty() || file.tags.any { tag -> tag in filters.tags }) &&
                    (filters.folders.isEmpty() || file.folderWithSpaces in filters.folders) &&
                    (filters.publics.isEmpty() || file.publicEnum in filters.publics)
        }.sortedBy { it.rpath }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val title: StateFlow<String> = filterStateFlow.map { filters ->
        buildTitle(filters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

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

    fun updateFiles() {
        viewModelScope.launch {
            val file = settingsRepository.musicDatafile.firstOrNull()
            val root = settingsRepository.musicRootFolder.firstOrNull()

            if (file != null && root != null) {
                musicRepository.loadMusicData(datafileUri = file, rootFolderUri = root, clearCache = true)
            }
        }
    }

    fun resetFilters() {
        authorFilter = ""
        nameFilter = ""
        selectedMoods.clear()
        selectedMoods.addAll(MusicMood.entries)
        selectedLikes.clear()
        selectedLikes.addAll(MusicLike.entries)
        selectedLangs.clear()
        selectedLangs.addAll(MusicLang.entries)
        selectedEmos.clear()
        selectedEmos.addAll(MusicEmo.entries)
        selectedTags.clear()
        selectedFolders.clear()
        selectedPublics.clear()
    }

    private fun buildTitle(filters: FilterState): String {
        val parts = mutableListOf<String>()

        parts.add(
            listOf(filters.author.take(10), filters.name.take(10))
                .filter { it.isNotEmpty() }
                .joinToString(" "))

        val ftags = mutableListOf<String>()
        fun <T : Enum<T>> tagToStr(allTags: List<T>, curTags: List<T>, k: Int, prefix: String) {
            if (curTags.size != allTags.size) {
                ftags.add(
                    prefix +
                            if (curTags.size < allTags.size - k)
                                curTags.joinToString("") { it.name.take(2) }
                            else
                                (allTags - curTags.toSet()).joinToString("") { "-${it.name.take(2)}" }
                )
            }
        }
        tagToStr(MusicMood.entries, filters.moods, 2, "M:")
        tagToStr(MusicLike.entries, filters.likes, 1, "L:")
        tagToStr(MusicLang.entries, filters.langs, 4, "N:")
        tagToStr(MusicEmo.entries, filters.emos, 1, "E:")

        parts.add(ftags.joinToString("; "))
        parts.add(filters.tags.joinToString(";"))
        parts.add(
            if (filters.folders.size < 2) filters.folders.joinToString(";")
            else "${filters.folders.size} folders"
        )

        parts.add(
            when {
                filters.publics.size == 1 && MusicPublic.Public in filters.publics -> "P"
                filters.publics.size == 1 && MusicPublic.Private in filters.publics -> "H"
                else -> ""
            }
        )

        return parts.filter { it != "" }.joinToString(" | ")
    }
}
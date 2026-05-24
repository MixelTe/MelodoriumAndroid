package com.mixelte.melodorium.domain

import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.domain.models.MusicEmo
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.domain.models.MusicPublic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

data class FilterState(
    val authorQuery: String = "",
    val nameQuery: String = "",
    val selectedMoods: List<MusicMood> = MusicMood.entries,
    val selectedLikes: List<MusicLike> = MusicLike.entries,
    val selectedLangs: List<MusicLang> = MusicLang.entries,
    val selectedEmos: List<MusicEmo> = MusicEmo.entries,
    val selectedTags: List<String> = emptyList(),
    val selectedFolders: List<String> = emptyList(),
    val selectedPublics: List<MusicPublic> = emptyList()
)

class MusicFilterManager(
    private val musicRepository: MusicRepository
) {
    private val _state = MutableStateFlow(FilterState())
    val state: StateFlow<FilterState> = _state.asStateFlow()

    val filteredFiles = combine(
        musicRepository.files,
        _state
    ) { files, filters ->
        if (files.isEmpty()) return@combine emptyList()
        val authorLower = filters.authorQuery.lowercase()
        val nameLower = filters.nameQuery.lowercase()

        files.filter { file ->
            (authorLower.isEmpty() || authorLower in file.authorNorm) &&
                    (nameLower.isEmpty() || nameLower in file.nameNorm) &&
                    (filters.selectedMoods.isEmpty() || file.mood in filters.selectedMoods) &&
                    (filters.selectedLikes.isEmpty() || file.like in filters.selectedLikes) &&
                    (filters.selectedLangs.isEmpty() || file.lang in filters.selectedLangs) &&
                    (filters.selectedEmos.isEmpty() || file.emo in filters.selectedEmos) &&
                    (filters.selectedTags.isEmpty() || file.tags.any { tag -> tag in filters.selectedTags }) &&
                    (filters.selectedFolders.isEmpty() || file.folderWithSpaces in filters.selectedFolders) &&
                    (filters.selectedPublics.isEmpty() || file.publicEnum in filters.selectedPublics)
        }.sortedBy { it.rpath }
    }

    val title = _state.map { filters -> buildTitle(filters) }

    fun updateAuthorQuery(query: String) = _state.update { it.copy(authorQuery = query) }
    fun updateNameQuery(query: String) = _state.update { it.copy(nameQuery = query) }

    fun toggleMood(mood: MusicMood, isSelected: Boolean? = null) = _state.update { s ->
        s.copy(selectedMoods = updateCollection(s.selectedMoods, mood, isSelected))
    }

    fun toggleLike(like: MusicLike, isSelected: Boolean? = null) = _state.update { s ->
        s.copy(selectedLikes = updateCollection(s.selectedLikes, like, isSelected))
    }

    fun toggleLang(lang: MusicLang, isSelected: Boolean? = null) = _state.update { s ->
        s.copy(selectedLangs = updateCollection(s.selectedLangs, lang, isSelected))
    }

    fun toggleEmo(emo: MusicEmo, isSelected: Boolean? = null) = _state.update { s ->
        s.copy(selectedEmos = updateCollection(s.selectedEmos, emo, isSelected))
    }

    fun toggleTag(tag: String, isSelected: Boolean? = null) = _state.update { s ->
        s.copy(selectedTags = updateCollection(s.selectedTags, tag, isSelected))
    }

    fun toggleFolder(folder: String, isSelected: Boolean? = null) = _state.update { s ->
        s.copy(selectedFolders = updateCollection(s.selectedFolders, folder, isSelected))
    }

    fun togglePublic(public: MusicPublic, isSelected: Boolean? = null) = _state.update { s ->
        s.copy(selectedPublics = updateCollection(s.selectedPublics, public, isSelected))
    }

    fun reset() {
        _state.value = FilterState()
    }

    private fun <T> updateCollection(current: Collection<T>, item: T, isSelected: Boolean?): List<T> {
        val shouldSelect = isSelected ?: (item !in current)

        return if (shouldSelect) {
            if (item in current) current.toList() else current + item
        } else {
            current - item
        }
    }

    private fun buildTitle(filters: FilterState): String {
        val parts = mutableListOf<String>()

        parts.add(
            listOf(filters.authorQuery.take(10), filters.nameQuery.take(10))
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
        tagToStr(MusicMood.entries, filters.selectedMoods, 2, "M:")
        tagToStr(MusicLike.entries, filters.selectedLikes, 1, "L:")
        tagToStr(MusicLang.entries, filters.selectedLangs, 4, "N:")
        tagToStr(MusicEmo.entries, filters.selectedEmos, 1, "E:")

        parts.add(ftags.joinToString("; "))
        parts.add(filters.selectedTags.joinToString(";"))
        parts.add(
            if (filters.selectedFolders.size < 2) filters.selectedFolders.joinToString(";")
            else "${filters.selectedFolders.size} folders"
        )

        parts.add(
            when {
                filters.selectedPublics.size == 1 && MusicPublic.Public in filters.selectedPublics -> "P"
                filters.selectedPublics.size == 1 && MusicPublic.Private in filters.selectedPublics -> "H"
                else -> ""
            }
        )

        return parts.filter { it != "" }.joinToString(" | ")
    }
}
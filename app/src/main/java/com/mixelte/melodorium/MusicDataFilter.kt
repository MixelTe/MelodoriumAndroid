package com.mixelte.melodorium

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MusicDataFilter(scope: CoroutineScope) {
    var author by mutableStateOf("")
    var name by mutableStateOf("")
    var mood = MusicMood.entries.toMutableStateList()
    var like = MusicLike.entries.toMutableStateList()
    var lang = MusicLang.entries.toMutableStateList()
    var emo = MusicEmo.entries.toMutableStateList()

    var files by mutableStateOf<List<MusicFile>>(listOf())

    init {
        snapshotFlow { MusicData.Files }.onEach { updateFiles() }.launchIn(scope)
        snapshotFlow { "$author$name" }.onEach { updateFiles() }.launchIn(scope)
        snapshotFlow { mood.toList() }.onEach { updateFiles() }.launchIn(scope)
        snapshotFlow { like.toList() }.onEach { updateFiles() }.launchIn(scope)
        snapshotFlow { lang.toList() }.onEach { updateFiles() }.launchIn(scope)
        snapshotFlow { emo.toList() }.onEach { updateFiles() }.launchIn(scope)
    }

    private fun updateFiles() {
        val authorLower = author.lowercase()
        val nameLower = name.lowercase()
        files = MusicData.Files.filter {
            (authorLower == "" || authorLower in it.authorNorm) &&
                    (nameLower == "" || nameLower in it.nameNorm) &&
                    (mood.isEmpty() || it.mood in mood) &&
                    (like.isEmpty() || it.like in like) &&
                    (lang.isEmpty() || it.lang in lang) &&
                    (emo.isEmpty() || it.emo in emo)
        }
    }

    fun reset() {
        author = ""
        name = ""
        mood = MusicMood.entries.toMutableStateList()
        like = MusicLike.entries.toMutableStateList()
        lang = MusicLang.entries.toMutableStateList()
        emo = MusicEmo.entries.toMutableStateList()
    }
}
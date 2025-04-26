package com.mixelte.melodorium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList

object MusicDataFilter {
    var author by mutableStateOf("")
    var name by mutableStateOf("")
    var mood = MusicMood.entries.toMutableStateList()
    var like = MusicLike.entries.toMutableStateList()
    var lang = MusicLang.entries.toMutableStateList()
    var emo = MusicEmo.entries.toMutableStateList()

    var files by mutableStateOf<List<MusicFile>>(listOf())
    var title by mutableStateOf("")

    @Composable
    fun Updater() {
        LaunchedEffect(MusicData.Files, author, name, mood.toList(), like.toList(), lang.toList(), emo.toList()) {
            updateFiles()
        }
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
        title = buildTitle()
    }

    fun reset() {
        author = ""
        name = ""
        mood.clear()
        mood.addAll(MusicMood.entries)
        like.clear()
        like.addAll(MusicLike.entries)
        lang.clear()
        lang.addAll(MusicLang.entries)
        emo.clear()
        emo.addAll(MusicEmo.entries)
    }

    private fun buildTitle(): String {
        var title = ""
        if (author != "")
            title += author.take(10)
        if (name != "")
            title += (if (title != "") " " else "") + name.take(10)

        val tags = mutableStateListOf<String>()
        fun <T : Enum<T>> tagToStr(allTags: List<T>, curTags: List<T>, k: Int, prefix: String) {
            if (curTags.size != allTags.size) {
                if (curTags.size < allTags.size - k)
                    tags.add(prefix + curTags.joinToString("") { it.name.take(2) })
                else
                    tags.add(prefix + (allTags - curTags.toSet()).joinToString("") { "-${it.name.take(2)}" })
            }
        }
        tagToStr(MusicMood.entries, mood, 2, "M:")
        tagToStr(MusicLike.entries, like, 1, "L:")
        tagToStr(MusicLang.entries, lang, 4, "N:")
        tagToStr(MusicEmo.entries, emo, 1, "E:")

        if (tags.isNotEmpty()) {
            if (title != "") title += " "
            title += tags.joinToString("; ")
        }
        return title
    }
}
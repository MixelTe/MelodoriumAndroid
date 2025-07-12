package com.mixelte.melodorium.data

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
    var tags = mutableStateListOf<String>()
    var folders = mutableStateListOf<String>()
    var public = mutableStateListOf<MusicPublic>()

    var files by mutableStateOf<List<MusicFile>>(listOf())
    var title by mutableStateOf("")

    @Composable
    fun Updater() {
        LaunchedEffect(
            MusicData.Files,
            author,
            name,
            mood.toList(),
            like.toList(),
            lang.toList(),
            emo.toList(),
            tags.toList(),
            folders.toList(),
            public.toList(),
        ) {
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
                    (emo.isEmpty() || it.emo in emo) &&
                    (tags.isEmpty() || it.tags.any { tag -> tag in tags }) &&
                    (folders.isEmpty() || it.folderWithSpaces in folders) &&
                    (public.isEmpty() || it.publicEnum in public)
        }.sortedBy { it.rpath }
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
        tags.clear()
        folders.clear()
        public.clear()
    }

    private fun buildTitle(): String {
        val parts = mutableListOf<String>()
        parts.add(listOf(author.take(10), name.take(10)).joinToString(" "))

        val ftags = mutableStateListOf<String>()
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
        tagToStr(MusicMood.entries, mood, 2, "M:")
        tagToStr(MusicLike.entries, like, 1, "L:")
        tagToStr(MusicLang.entries, lang, 4, "N:")
        tagToStr(MusicEmo.entries, emo, 1, "E:")

        parts.add(ftags.joinToString("; "))
        parts.add(tags.joinToString(";"))
        parts.add(
            if (folders.size < 2) folders.joinToString(";")
            else "${folders.size} folders"
        )
        parts.add(
            when {
                public.size == 1 && MusicPublic.Public in public -> "P"
                public.size == 1 && MusicPublic.Private in public -> "H"
                else -> ""
            }
        )

        return parts.filter { it != "" }.joinToString(" | ")
    }
}
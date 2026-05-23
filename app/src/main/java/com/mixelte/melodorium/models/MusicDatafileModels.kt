package com.mixelte.melodorium.models

import kotlinx.serialization.Serializable


@Serializable
data class MusicDatafile(
    @Suppress("PropertyName") var Version: Int,
    @Suppress("PropertyName") var FolderAuthor: Map<String, String>,
    @Suppress("PropertyName") var Files: List<MusicFileData>,
)

@Serializable
data class MusicFileData(
    @Suppress("PropertyName") var RPath: String,
    @Suppress("PropertyName") val IsLoaded: Boolean,
    @Suppress("PropertyName") val Mood: MusicMood,
    @Suppress("PropertyName") val Like: MusicLike,
    @Suppress("PropertyName") val Lang: MusicLang,
    @Suppress("PropertyName") val Emo: MusicEmo,
    @Suppress("PropertyName") val Public: Boolean,
    @Suppress("PropertyName") val Tag: String,
)

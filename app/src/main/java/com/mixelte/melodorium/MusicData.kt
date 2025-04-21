package com.mixelte.melodorium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

object MusicData {
    var Files: List<MusicFile> = listOf()
        set(value) {
            field = value
            FilesState.value = value
        }
    var FolderAuthor: Map<String, String> = mapOf()
    var Error: String? = null
        set(value) {
            field = value
            ErrorState.value = value
        }

    var FilesState = mutableStateOf(Files)
    var ErrorState = mutableStateOf(Error)

    @Composable
    fun MusicDataLoader() {
        val musicDatafile = getMusicDatafile()
        val musicRootFolder = getMusicRootFolder()
        val context = LocalContext.current

        LaunchedEffect(musicDatafile, musicRootFolder) {
            try {
                val datafile = musicDatafile?.let { DocumentFile.fromSingleUri(context, it) } ?: return@LaunchedEffect
                val inputStream = context.contentResolver.openInputStream(datafile.uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readText()
                val withUnknownKeys = Json { ignoreUnknownKeys = true }
                val obj = withUnknownKeys.decodeFromString(MusicDatafile.serializer(), lines)
                FolderAuthor = obj.FolderAuthor

                val directory = musicRootFolder?.let { DocumentFile.fromTreeUri(context, it) }
                Files = directory?.listFiles()?.map {
                    MusicFile(it.name)
                } ?: listOf()
            } catch (e: Exception) {
                Error = e.toString()
                return@LaunchedEffect
            }
            Error = null
        }
    }
}

@Serializable
data class MusicDatafile(
    var Version: Int,
    var FolderAuthor: Map<String, String>,
    var Files: List<MusicFileData>,
)
@Serializable
data class MusicFileData (
    val RPath: String,
    val IsLoaded: Boolean,
    val Mood: MusicMood,
    val Like: MusicLike,
    val Lang: MusicLang,
    val Emo: MusicEmo,
    val Hidden: Boolean,
    val Tag: String,
)

data class MusicFile(val name: String?) {
    val Path = ""
    val Mood = MusicMood.Energistic
    val Like = MusicLike.Good
    val Lang = MusicLang.An
    val Emo = MusicEmo.Neutral
    val Hidden = false
    val Tag = ""

    val Tags: String
        get() {
            var tags = ""
            tags += " ["
            tags += Mood.name.substring(0, 2)
            if (Emo == MusicEmo.Happy) tags += "+"
            else if (Emo == MusicEmo.Sad) tags += "-"
            tags += ";"
            tags += Like.name.substring(0, 2) + ";"
            tags += Lang.name.substring(0, 2)
            if (Tag != "")
                tags += "|" + Tag
            tags += "]"
            return tags
        }
}

enum class MusicMood
{
    Rock,
    Energistic,
    Cheerful,
    Calm,
    Sleep,
}
enum class MusicLike
{
    Best,
    Like,
    Good,
    Normal,
}
enum class MusicLang
{
    No,
    Ru,
    An,
    En,
    Fr,
    Ge,
    It,
    As,
}
enum class MusicEmo
{
    Happy,
    Neutral,
    Sad,
}
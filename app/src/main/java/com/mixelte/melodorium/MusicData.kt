package com.mixelte.melodorium

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

object MusicData {
    var FolderAuthor: Map<String, String> = mapOf()
    var Files by mutableStateOf(listOf<MusicFile>())
    var Error by mutableStateOf<String?>(null)
    var IsLoading by mutableStateOf(false)

    private var curMusicDatafile = ""
    private var curMusicRootFolder = ""

    @Composable
    fun MusicDataLoader() {
        val musicDatafile = getMusicDatafile()
        val musicRootFolder = getMusicRootFolder()
        val context = LocalContext.current

        LaunchedEffect(musicDatafile, musicRootFolder) {
            if (musicDatafile == null || musicRootFolder == null) return@LaunchedEffect
            val newMusicDatafile = musicDatafile.toString()
            val newMusicRootFolder = musicRootFolder.toString()
            if (curMusicDatafile == newMusicDatafile && curMusicRootFolder == newMusicRootFolder) return@LaunchedEffect
            curMusicDatafile = newMusicDatafile
            curMusicRootFolder = newMusicRootFolder
            withContext(Dispatchers.IO) {
                try {
                    Files = listOf()
                    IsLoading = true
                    val datafile = DocumentFile.fromSingleUri(context, musicDatafile)
                        ?: return@withContext
                    val inputStream = context.contentResolver.openInputStream(datafile.uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val lines = reader.readText()
                    val withUnknownKeys = Json { ignoreUnknownKeys = true }
                    val obj = withUnknownKeys.decodeFromString(MusicDatafile.serializer(), lines)
                    obj.Files.map { file -> file.RPath = file.RPath.replace("\\", "/") }
                    FolderAuthor = obj.FolderAuthor

                    val directory = DocumentFile.fromTreeUri(context, musicRootFolder)
                        ?: return@withContext
                    Files = scanDirectory(directory, obj).sortedBy { it.rpath }.toList()
                } catch (e: Exception) {
                    Error = e.toString()
                    return@withContext
                } finally {
                    IsLoading = false
                }
                Error = null
            }
        }
    }

    private fun scanDirectory(
        directory: DocumentFile,
        datafile: MusicDatafile,
        path: String = "",
    ): MutableList<MusicFile> {
        val files: MutableList<MusicFile> = mutableListOf()
        directory.listFiles().map {
            if (it.isDirectory) {
                files.addAll(scanDirectory(it, datafile, path + it.uri.getFilename() + "/"))
            } else {
                val fname = it.uri.getFilename()
                val rpath = path + fname
                val data = datafile.Files.find { file -> file.RPath == rpath } ?: return@map
                if (data.IsLoaded) {
                    files.add(MusicFile(it, path.trimEnd('/'), fname, data))
                }
            }
        }
        return files
    }
}

private fun Uri.getFilename(): String {
    val path = this.path ?: return ""
    val cut = path.lastIndexOf('/')
    if (cut != -1) return path.substring(cut + 1)
    return path
}

@Serializable
data class MusicDatafile(
    var Version: Int,
    var FolderAuthor: Map<String, String>,
    var Files: List<MusicFileData>,
)

@Serializable
data class MusicFileData(
    var RPath: String,
    val IsLoaded: Boolean,
    val Mood: MusicMood,
    val Like: MusicLike,
    val Lang: MusicLang,
    val Emo: MusicEmo,
    val Hidden: Boolean,
    val Tag: String,
)

class MusicFile(
    val file: DocumentFile,
    directory: String,
    fname: String,
    data: MusicFileData,
) {
    val name: String
    val author: String
    val nameNorm: String
    val authorNorm: String
    val rpath: String = "$directory/$fname"
    val ext: String
    val mood = data.Mood
    val like = data.Like
    val lang = data.Lang
    val emo = data.Emo
    val hidden = data.Hidden
    val tag = data.Tag

    init {
        val exti = fname.lastIndexOf(".")
        val sname = (if (exti >= 0) fname.substring(0, exti) else fname)
        ext = if (exti >= 0) fname.substring(exti + 1) else fname
        if ("_-_" in sname) {
            val parts = sname.split("_-_")
            author = parts[0].replace("_", " ")
            name = parts[1].replace("_", " ")
        } else {
            author = ""
            name = sname.replace("_", " ")
        }
        nameNorm = name.lowercase()
        authorNorm = author.lowercase()
    }

    val tags: String
        get() {
            var tags = ""
            tags += " ["
            tags += mood.name.substring(0, 2)
            if (emo == MusicEmo.Happy) tags += "+"
            else if (emo == MusicEmo.Sad) tags += "-"
            tags += ";"
            tags += like.name.substring(0, 2) + ";"
            tags += lang.name.substring(0, 2)
            if (tag != "")
                tags += "|$tag"
            tags += "]"
            return tags
        }
}

enum class MusicMood {
    Rock,
    Energistic,
    Cheerful,
    Calm,
    Sleep,
}

enum class MusicLike {
    Best,
    Like,
    Good,
    Normal,
}

enum class MusicLang {
    No,
    Ru,
    An,
    En,
    Fr,
    Ge,
    It,
    As,
}

enum class MusicEmo {
    Happy,
    Neutral,
    Sad,
}
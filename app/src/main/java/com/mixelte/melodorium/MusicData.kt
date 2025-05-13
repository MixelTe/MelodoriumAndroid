package com.mixelte.melodorium

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.room.Room
import com.mixelte.melodorium.db.AppDatabase
import com.mixelte.melodorium.db.File
import com.mixelte.melodorium.db.FileDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader


object MusicData {
    var Files by mutableStateOf(listOf<MusicFile>())
    var Tags by mutableStateOf(listOf<String>())
    var Error by mutableStateOf<String?>(null)
    var IsLoading by mutableStateOf(false)

    private var curMusicDatafile = ""
    private var curMusicRootFolder = ""
    private var curMusicRootFolderUri: Uri? = null;
    private var musicDatafile: MusicDatafile? = null;

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
            curMusicRootFolderUri = musicRootFolder
            withContext(Dispatchers.IO) {
                try {
                    Files = listOf()
                    Tags = listOf()
                    IsLoading = true
                    Error = null
                    val datafile = DocumentFile.fromSingleUri(context, musicDatafile)
                        ?: return@withContext
                    val inputStream = context.contentResolver.openInputStream(datafile.uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val lines = reader.readText()
                    val withUnknownKeys = Json { ignoreUnknownKeys = true }
                    val obj = withUnknownKeys.decodeFromString(MusicDatafile.serializer(), lines)
                    MusicData.musicDatafile = obj

                    obj.Files.forEach { it.RPath = it.RPath.replace("\\", "/") }

                    loadFiles(context, musicRootFolder, obj.Files)

                    val tags = mutableSetOf<String>()
                    Files.forEach { it.tags.forEach { tags.add(it) } }
                    tags.remove("")
                    Tags = tags.toList()
                } catch (e: Exception) {
                    Error = e.toString()
                } finally {
                    IsLoading = false
                }
            }
        }
    }

    suspend fun updateFiles(context: Context) {
        curMusicRootFolderUri?.let { rootUri ->
            musicDatafile?.let { dataFile ->
                withContext(Dispatchers.IO) {
                    IsLoading = true
                    Error = null
                    try {
                        loadFiles(context, rootUri, dataFile.Files, useCache = false)
                    } catch (e: Exception) {
                        Error = e.toString()
                    } finally {
                        IsLoading = false
                    }
                }
            }
        }
    }

    private fun loadFiles(
        context: Context,
        rootUri: Uri,
        musicData: List<MusicFileData>,
        useCache: Boolean = true
    ) {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database-name"
        ).build()
        try {
            val fileDao = db.fileDao()
            (if (useCache) fileDao.getAll() else null)?.let { filesCache ->
                if (filesCache.isEmpty()) null else loadFilesFromCache(filesCache, musicData)
            } ?: loadFilesFromSystem(fileDao, context, rootUri, musicData)
        } finally {
            db.close()
        }
    }

    private fun loadFilesFromCache(filesCache: List<File>, musicData: List<MusicFileData>) {
        val files = mutableListOf<MusicFile>()
        for (file in filesCache) {
            val data = musicData.find { it.RPath == file.rpath }
            if (data?.IsLoaded == true) {
                files.add(MusicFile(data, file.uri.toUri()))
            }
        }
        Files = files
    }

    private fun loadFilesFromSystem(
        fileDao: FileDao,
        context: Context,
        rootUri: Uri,
        musicData: List<MusicFileData>
    ) {
        val files = mutableListOf<MusicFile>()
        val cache = mutableListOf<File>()
        val contentResolver = context.contentResolver

        val dirNodes = mutableListOf(
            "" to DocumentsContract.buildChildDocumentsUriUsingTree(
                rootUri,
                DocumentsContract.getTreeDocumentId(rootUri)
            )
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        while (!dirNodes.isEmpty()) {
            val (path, dirUri) = dirNodes.removeAt(0)
            contentResolver.query(dirUri, projection, null, null, null)?.apply {
                try {
                    while (moveToNext()) {
                        val docId = getString(0)
                        val name = getString(1)
                        val mime = getString(2)
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            dirNodes.add(
                                "$path/$name" to DocumentsContract.buildChildDocumentsUriUsingTree(
                                    rootUri,
                                    docId
                                )
                            )
                        } else {
                            val rpath = "$path/$name".trimStart('/')
                            val data = musicData.find { it.RPath == rpath }
                            if (data?.IsLoaded == true) {
                                val uri = DocumentsContract.buildDocumentUriUsingTree(
                                    rootUri,
                                    docId
                                )
                                files.add(MusicFile(data, uri))
                                cache.add(File(0, rpath, uri.toString()))
                            }
                        }
                    }
                } finally {
                    close()
                }
            }
        }
        fileDao.deleteAll()
        fileDao.insertAll(cache)
        Files = files
    }
}

private fun String.getFilename(): String {
    val cut = this.lastIndexOf('/')
    if (cut != -1) return this.substring(cut + 1)
    return this
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
    data: MusicFileData,
    val uri: Uri,
) {
    val name: String
    val author: String
    val nameNorm: String
    val authorNorm: String
    val rpath: String = data.RPath
    val ext: String
    val mood = data.Mood
    val like = data.Like
    val lang = data.Lang
    val emo = data.Emo
    val hidden = data.Hidden
    val tag = data.Tag
    val tags = data.Tag.split(";")

    init {
        val fname = rpath.getFilename()
        val exti = fname.lastIndexOf(".")
        val sname = if (exti >= 0) fname.substring(0, exti) else fname
        ext = if (exti >= 0) fname.substring(exti + 1) else ""
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

    val tagsLabel: String
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
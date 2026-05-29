package com.mixelte.melodorium.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.mixelte.melodorium.data.db.AppDatabase
import com.mixelte.melodorium.data.db.CurrentPlaylistEntity
import com.mixelte.melodorium.data.db.FileEntity
import com.mixelte.melodorium.data.db.PlaybackStateEntity
import com.mixelte.melodorium.domain.models.MusicDatafile
import com.mixelte.melodorium.domain.models.MusicFile
import com.mixelte.melodorium.domain.models.MusicFileData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

data class PlaylistEntry(
    val id: Long,
    val file: MusicFile
)

class MusicRepository(
    context: Context,
    private val database: AppDatabase
) {
    private val context = context.applicationContext
    private val fileDao = database.fileDao()
    private val playlistDao = database.playlistDao()

    private val _files = MutableStateFlow<List<MusicFile>?>(null)
    val files: StateFlow<List<MusicFile>?> = _files.asStateFlow()

    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    private val _authors = MutableStateFlow<List<String>>(emptyList())
    val authors: StateFlow<List<String>> = _authors.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentPlaylist: Flow<List<PlaylistEntry>> =
        combine(playlistDao.getCurrentPlaylistFlow(), _files) { entities, allFiles ->
            Pair(entities, allFiles)
        }
            .filter { (_, files) -> files != null }
            .map { (entities, allFiles) ->
                val filesMap = allFiles!!.associateBy { it.rpath }
                entities.mapNotNull { entity ->
                    val file = filesMap[entity.rpath]
                    file?.let { PlaylistEntry(entity.id, it) }
                }
            }

    suspend fun saveCurrentPlaylist(entries: List<PlaylistEntry>) {
        val entities = entries.mapIndexed { index, entry ->
            CurrentPlaylistEntity(
                id = entry.id, // id == 0 -> new
                rpath = entry.file.rpath,
                position = index
            )
        }
        playlistDao.savePlaylist(entities)
    }

    suspend fun getPlaybackState() = playlistDao.getPlaybackState()

    suspend fun updatePlaybackState(rpath: String?, positionMs: Long, isPlaying: Boolean) {
        playlistDao.savePlaybackState(
            PlaybackStateEntity(currentTrackRpath = rpath, currentPositionMs = positionMs, isPlaying = isPlaying)
        )
    }

    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun loadMusicData(datafileUri: Uri, rootFolderUri: Uri, clearCache: Boolean = false) =
        withContext(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            try {
                val datafile =
                    DocumentFile.fromSingleUri(context, datafileUri) ?: return@withContext
                val inputStream = context.contentResolver.openInputStream(datafile.uri)
                val lines =
                    inputStream?.bufferedReader()?.use { it.readText() } ?: return@withContext

                val obj = jsonParser.decodeFromString(MusicDatafile.serializer(), lines)
                obj.Files.forEach { it.RPath = it.RPath.replace("\\", "/") }

                val cachedFiles = fileDao.getAll()
                if (clearCache) {
                    context.filesDir.resolve("artworks").deleteRecursively()
                    fileDao.deleteAll()
                }

                val loadedFiles = if (cachedFiles.isNotEmpty() && !clearCache) {
                    loadFromCache(cachedFiles, obj.Files)
                } else {
                    loadFromSystemAndCache(cachedFiles, rootFolderUri, obj.Files)
                }

                _files.value = loadedFiles.sortedBy { it.rpath }

                val tagsDeferred = async(Dispatchers.Default) {
                    loadedFiles.flatMap { it.tags }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
                }
                val foldersDeferred = async(Dispatchers.Default) {
                    loadedFiles.map { it.folder.replace('_', ' ') }.distinct().sortedWith(String.CASE_INSENSITIVE_ORDER)
                }
                val authorsDeferred = async(Dispatchers.Default) {
                    loadedFiles.map { it.author }.filter { it.isNotEmpty() }.distinct()
                        .sortedWith(String.CASE_INSENSITIVE_ORDER)
                }

                _tags.value = tagsDeferred.await()
                _folders.value = foldersDeferred.await()
                _authors.value = authorsDeferred.await()

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: e.toString()
            } finally {
                _isLoading.value = false
            }
        }

    private fun loadFromCache(
        filesCache: List<FileEntity>,
        musicData: List<MusicFileData>
    ): List<MusicFile> {
        val musicDataMap = musicData.associateBy { it.RPath }
        return filesCache.mapNotNull { cached ->
            val data = musicDataMap[cached.rpath]
            if (data?.IsLoaded == true) {
                val artFile = cached.artworkPath?.let { File(it) }
                MusicFile.create(data, cached.uri.toUri(), artFile)
            } else null
        }
    }

    private suspend fun loadFromSystemAndCache(
        cachedFiles: List<FileEntity>,
        rootUri: Uri,
        musicData: List<MusicFileData>,
    ): List<MusicFile> = withContext(Dispatchers.IO) {
        val musicDataMap = musicData.associateBy { it.RPath }
        val oldFilesMap = cachedFiles.associateBy { it.rpath }

        val files = mutableListOf<MusicFile>()
        val cache = mutableListOf<FileEntity>()
        val contentResolver = context.contentResolver

        val dirNodes = ArrayDeque<Pair<String, Uri>>().apply {
            add(
                "" to DocumentsContract.buildChildDocumentsUriUsingTree(
                    rootUri,
                    DocumentsContract.getTreeDocumentId(rootUri)
                )
            )
        }

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )

        while (dirNodes.isNotEmpty()) {
            val (path, dirUri) = dirNodes.removeFirstOrNull() ?: break

            contentResolver.query(dirUri, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext()) {
                    val docId = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex)
                    val mime = cursor.getString(mimeIndex)

                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        val nextPath = if (path.isEmpty()) name else "$path/$name"
                        dirNodes.add(
                            nextPath to DocumentsContract.buildChildDocumentsUriUsingTree(
                                rootUri,
                                docId
                            )
                        )
                    } else {
                        val relPath = if (path.isEmpty()) name else "$path/$name"
                        val data = musicDataMap[relPath]
                        if (data?.IsLoaded == true) {
                            val uri = DocumentsContract.buildDocumentUriUsingTree(rootUri, docId)

                            val existingArtworkPath = oldFilesMap[relPath]?.artworkPath
                            val artFile = existingArtworkPath?.let { File(it) }

                            files.add(MusicFile.create(data, uri, artFile))
                            cache.add(
                                FileEntity(
                                    rpath = relPath,
                                    uri = uri.toString(),
                                    artworkPath = existingArtworkPath
                                )
                            )
                        }
                    }
                }
            }
        }

        val newFilesMap = cache.associateBy { it.rpath }
        val toDelete = cachedFiles.filter { it.rpath !in newFilesMap }
        val toAdd = cache.filter { it.rpath !in oldFilesMap || oldFilesMap[it.rpath]?.uri != it.uri }

        if (toDelete.isNotEmpty()) fileDao.deleteMultiple(toDelete)
        if (toAdd.isNotEmpty()) fileDao.insertAll(toAdd)

        return@withContext files
    }

    suspend fun getArtworkFile(musicFile: MusicFile): File? = withContext(Dispatchers.IO) {
        if (musicFile.artworkFile != null) {
            return@withContext musicFile.artworkFile
        }

        val extractedPath = extractAndCacheArtwork(musicFile.uri, musicFile.rpath)

        if (extractedPath != null) {
            fileDao.updateArtworkPath(musicFile.rpath, extractedPath)

            _files.value?.let { currentFiles ->
                val index = currentFiles.indexOfFirst { it.rpath == musicFile.rpath }
                if (index != -1) {
                    val mutableList = currentFiles.toMutableList()
                    mutableList[index] = mutableList[index].copy(artworkFile = File(extractedPath))
                    _files.value = mutableList
                }
            }
        }

        return@withContext extractedPath?.let { File(it) }
    }

    private fun extractAndCacheArtwork(fileUri: Uri, relativePath: String): String? {
        val retriever = MediaMetadataRetriever()
        try {
            context.contentResolver.openFileDescriptor(fileUri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
                val embeddedPicture = retriever.embeddedPicture

                if (embeddedPicture != null) {
                    val artDir = File(context.filesDir, "artworks").apply { mkdirs() }

                    val fileName = relativePath.md5() + ".jpg"
                    val artFile = File(artDir, fileName)

                    FileOutputStream(artFile).use { fos ->
                        fos.write(embeddedPicture)
                    }
                    return artFile.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(this.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
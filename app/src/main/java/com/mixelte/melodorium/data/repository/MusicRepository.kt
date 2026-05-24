package com.mixelte.melodorium.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.mixelte.melodorium.data.db.AppDatabase
import com.mixelte.melodorium.data.db.FileDao
import com.mixelte.melodorium.data.db.FileEntity
import com.mixelte.melodorium.domain.models.MusicDatafile
import com.mixelte.melodorium.domain.models.MusicFile
import com.mixelte.melodorium.domain.models.MusicFileData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MusicRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val _files = MutableStateFlow<List<MusicFile>>(emptyList())
    val files: StateFlow<List<MusicFile>> = _files.asStateFlow()

    private val _folders = MutableStateFlow<List<String>>(emptyList())
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

                val fileDao = database.fileDao()
                val cachedFiles = fileDao.getAll()

                val loadedFiles = if (cachedFiles.isNotEmpty() && !clearCache) {
                    loadFromCache(cachedFiles, obj.Files)
                } else {
                    loadFromSystemAndCache(fileDao, rootFolderUri, obj.Files)
                }

                _files.value = loadedFiles

                _tags.value = loadedFiles
                    .flatMap { it.tags }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)

                _folders.value = loadedFiles
                    .map { it.folder.replace('_', ' ') }
                    .distinct()
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)

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
        return filesCache.mapNotNull { cached ->
            val data = musicData.find { it.RPath == cached.rpath }
            if (data?.IsLoaded == true) MusicFile(data, cached.uri.toUri()) else null
        }
    }

    private suspend fun loadFromSystemAndCache(
        fileDao: FileDao,
        rootUri: Uri,
        musicData: List<MusicFileData>
    ): List<MusicFile> = withContext(Dispatchers.IO) {
        val files = mutableListOf<MusicFile>()
        val cache = mutableListOf<FileEntity>()
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

        while (dirNodes.isNotEmpty()) {
            val (path, dirUri) = dirNodes.removeAt(0)

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
                        val data = musicData.find { it.RPath == relPath }
                        if (data?.IsLoaded == true) {
                            val uri = DocumentsContract.buildDocumentUriUsingTree(rootUri, docId)
                            files.add(MusicFile(data, uri))
                            cache.add(FileEntity(id = 0, rpath = relPath, uri = uri.toString()))
                        }
                    }
                }
            }
        }

        fileDao.deleteAll()
        fileDao.insertAll(cache)

        return@withContext files
    }
}
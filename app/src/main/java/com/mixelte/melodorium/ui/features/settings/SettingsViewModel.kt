package com.mixelte.melodorium.ui.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {
    val rootFolderUri: StateFlow<Uri?> = settingsRepository.musicRootFolder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val musicDatafileUri: StateFlow<Uri?> = settingsRepository.musicDatafile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoading = musicRepository.isLoading

    fun onRootFolderSelected(uri: Uri, context: Context) {
        takePersistablePermissions(context, uri)
        viewModelScope.launch { settingsRepository.saveRootFolder(uri) }
    }

    fun onDatafileSelected(uri: Uri, context: Context) {
        takePersistablePermissions(context, uri)
        viewModelScope.launch { settingsRepository.saveDatafile(uri) }
    }

    private fun takePersistablePermissions(context: Context, uri: Uri) {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    fun updateFiles() {
        viewModelScope.launch {
            val file = settingsRepository.musicDatafile.firstOrNull()
            val root = settingsRepository.musicRootFolder.firstOrNull()

            if (file != null && root != null) {
                musicRepository.loadMusicData(datafileUri = file, rootFolderUri = root, clearCache = true)
            }
        }
    }
}
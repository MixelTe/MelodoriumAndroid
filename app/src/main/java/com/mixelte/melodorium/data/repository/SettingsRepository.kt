package com.mixelte.melodorium.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mixelte.melodorium.ui.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    private val musicRootFolderKey = stringPreferencesKey("musicRootFolder")
    private val musicDatafileKey = stringPreferencesKey("musicDatafile")

    val musicRootFolder: Flow<Uri?> = context.dataStore.data.map { it[musicRootFolderKey]?.toUri() }
    val musicDatafile: Flow<Uri?> = context.dataStore.data.map { it[musicDatafileKey]?.toUri() }

    suspend fun saveRootFolder(uri: Uri) {
        context.dataStore.edit { it[musicRootFolderKey] = uri.toString() }
    }

    suspend fun saveDatafile(uri: Uri) {
        context.dataStore.edit { it[musicDatafileKey] = uri.toString() }
    }
}
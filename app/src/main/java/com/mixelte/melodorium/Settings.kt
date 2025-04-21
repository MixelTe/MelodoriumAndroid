package com.mixelte.melodorium

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


val Context.dataStore by preferencesDataStore("settings")

@Preview(backgroundColor = 0xFFFDE9F7, showBackground = true)
@Composable
fun Settings() {
    Column (modifier = Modifier.padding(5.dp)) {
        MusicRootFolder()
        MusicDatafile()
    }
}

val musicRootFolderKey = stringPreferencesKey("musicRootFolder")

@Composable
fun MusicRootFolder() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var folder = getMusicRootFolder()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        folder = it.data?.data.also {
            val contentResolver = context.contentResolver

            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(it!!, takeFlags)
        }
        scope.launch {
            context.dataStore.edit { preferences -> preferences[musicRootFolderKey] = folder.toString() }
        }
    }
    Column (
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            launcher.launch(intent)
        },
    ) {
        Text("Music root folder")
        Text(
            text = folder?.toString() ?: "unselected",
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun getMusicRootFolder(): Uri? {
    val context = LocalContext.current
    val flow: Flow<String?> = context.dataStore.data.map { it[musicRootFolderKey] }
    val state = flow.collectAsState(null).value
    return state?.let { Uri.parse(it) }
}

val musicDatafileKey = stringPreferencesKey("musicDatafile")

@Composable
fun MusicDatafile() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var file = getMusicDatafile()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        file = it.data?.data.also {
            val contentResolver = context.contentResolver

            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(it!!, takeFlags)
        }
        scope.launch {
            context.dataStore.edit { preferences -> preferences[musicDatafileKey] = file.toString() }
        }
    }
    Column (
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }

            launcher.launch(intent)
        },
    ) {
        Text("Music data file")
        Text(
            text = file?.toString() ?: "unselected",
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun getMusicDatafile(): Uri? {
    val context = LocalContext.current
    val flow: Flow<String?> = context.dataStore.data.map { it[musicDatafileKey] }
    val state = flow.collectAsState(null).value
    return state?.let { Uri.parse(it) }
}

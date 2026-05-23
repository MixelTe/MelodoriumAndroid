package com.mixelte.melodorium.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val rootFolder by viewModel.rootFolderUri.collectAsState()
    val datafile by viewModel.musicDatafileUri.collectAsState()
    val context = LocalContext.current

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri -> viewModel.onRootFolderSelected(uri, context) }
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri -> viewModel.onDatafileSelected(uri, context) }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().clickable {
            folderLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
        }) {
            Text("Music root folder")
            Text(text = rootFolder?.toString() ?: "unselected", modifier = Modifier.padding(start = 10.dp))
        }

        Column(modifier = Modifier.fillMaxWidth().clickable {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            fileLauncher.launch(intent)
        }) {
            Text("Music data file")
            Text(text = datafile?.toString() ?: "unselected", modifier = Modifier.padding(start = 10.dp))
        }
    }
}
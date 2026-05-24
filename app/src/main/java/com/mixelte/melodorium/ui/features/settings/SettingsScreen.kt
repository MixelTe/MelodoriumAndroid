package com.mixelte.melodorium.ui.features.settings


import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun SettingsRoute(viewModel: SettingsViewModel) {
    val rootFolderUri by viewModel.rootFolderUri.collectAsStateWithLifecycle()
    val musicDatafileUri by viewModel.musicDatafileUri.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri -> viewModel.onRootFolderSelected(uri, context) }
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri -> viewModel.onDatafileSelected(uri, context) }
    }

    SettingsScreen(
        rootFolderUri,
        musicDatafileUri,
        isLoading,
        onSelectRootFolder = {
            folderLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
        },
        onSelectDatafile = {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            fileLauncher.launch(intent)
        },
        onUpdateData = { viewModel.updateFiles() }
    )
}


@Composable
fun SettingsScreen(
    rootFolderUri: Uri?,
    musicDatafileUri: Uri?,
    isLoading: Boolean,
    onSelectRootFolder: () -> Unit,
    onSelectDatafile: () -> Unit,
    onUpdateData: () -> Unit,
) {
    val scrollState_rootFolder = rememberScrollState()
    LaunchedEffect(scrollState_rootFolder.maxValue) {
        if (scrollState_rootFolder.maxValue > 0) {
            scrollState_rootFolder.scrollTo(scrollState_rootFolder.maxValue)
        }
    }

    val scrollState_musicDatafile = rememberScrollState()
    LaunchedEffect(scrollState_musicDatafile.maxValue) {
        if (scrollState_musicDatafile.maxValue > 0) {
            scrollState_musicDatafile.scrollTo(scrollState_musicDatafile.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Данные:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState_rootFolder)
        ) {
            Text(
                text = rootFolderUri?.toString() ?: "не выбрано",
                fontStyle = if (rootFolderUri == null) FontStyle.Italic else null,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = onSelectRootFolder,
            modifier = Modifier.padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = "Папка",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Выбрать корневую папку",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState_musicDatafile)
        )
        {
            Text(
                text = musicDatafileUri?.toString() ?: "не выбрано",
                fontStyle = if (rootFolderUri == null) FontStyle.Italic else null,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSelectDatafile,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Файл",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Выбрать файл данных",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onUpdateData,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Обновить",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Обновить",
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text("Loading files", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF8F8)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(null, null, false, {}, {}, {})
}

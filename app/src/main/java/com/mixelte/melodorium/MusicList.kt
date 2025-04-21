package com.mixelte.melodorium

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile


@Composable
fun MusicList() {
    Column {
        MusicData.ErrorState.value?.let { Text(it) }
        LazyColumn {
            items(MusicData.FilesState.value) {
                Text(
                    text = it.name ?: "null"
                )
            }
        }
    }
}

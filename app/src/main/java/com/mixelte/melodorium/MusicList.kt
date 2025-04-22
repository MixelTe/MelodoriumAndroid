package com.mixelte.melodorium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.mixelte.melodorium.ui.theme.muted


@Composable
fun MusicList() {
    Column {
        MusicData.ErrorState.value?.let { Text(it) }
        LazyColumn {
            items(MusicData.FilesState.value) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(if (it.author != "") "${it.author}: ${it.name}" else it.name, modifier = Modifier.weight(1f))
                        Text(it.tags)
                    }
                    Text(
                        text = it.rpath,
                        modifier = Modifier.padding(start = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.muted
                    )
                }
            }
        }
    }
}

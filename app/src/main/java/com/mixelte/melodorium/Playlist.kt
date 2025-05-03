package com.mixelte.melodorium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.ui.theme.muted

@Composable
fun Playlist() {
    val current by Player.current.collectAsState()
    val playlist by Player.playlist.collectAsState()

    Column {
        Card (
            modifier = Modifier.padding(5.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
        ) {
            Column (
                modifier = Modifier.padding(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    current?.let {
                        Text(if (it.author != "") "${it.author}: ${it.name}" else it.name, modifier = Modifier.weight(1f))
                        Text(it.tags)
                    }
                    if (current == null) {
                        Text("No playing")
                    }
                }
                Text(
                    text = current?.rpath ?: "",
                    modifier = Modifier.padding(start = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.muted
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton({
                    Player.play()
                }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                }
            }
        }
        LazyColumn {
            items(playlist) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(if (it.author != "") "${it.author}: ${it.name}" else it.name, modifier = Modifier.weight(1f))
                    Text(it.tags)
                }
            }
        }
    }
}

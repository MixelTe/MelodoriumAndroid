package com.mixelte.melodorium.ui.playlist

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.player.PlayerItem
import com.mixelte.melodorium.ui.components.BetterLazyColumn
import com.mixelte.melodorium.ui.components.SwitchWithLabel
import com.mixelte.melodorium.ui.components.TextButtonSmall
import com.mixelte.melodorium.ui.theme.muted
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(viewModel: PlaylistViewModel) {
    val playlist by viewModel.playlist.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val mList = remember { BetterLazyColumn<PlayerItem>() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current

    Column {
        Card(
            modifier = Modifier.padding(5.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    currentTrack?.also {
                        Text(
                            if (it.file.author != "") "${it.file.author}: ${it.file.name}" else it.file.name,
                            modifier = Modifier.weight(1f),
                            minLines = 2,
                        )
                        Text(it.file.tagsLabel)
                    } ?: Text("No playing")
                }
                val rpath = currentTrack?.file?.rpath ?: ""
                Text(
                    text = rpath,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .clickable {
                            scope.launch {
                                val clipData = ClipData.newPlainText("label", rpath)
                                clipboardManager.setClipEntry(clipData.toClipEntry())
                            }
                        },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.muted
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton({ viewModel.prevTrack() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev")
                }
                IconButton({ viewModel.playPause() }) {
                    if (isPlaying) Icon(Icons.Filled.Pause, contentDescription = "Pause")
                    else Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                }
                IconButton({ viewModel.nextTrack() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SwitchWithLabel(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f),
                label = "Auto add next",
                checked = viewModel.autoAddNext,
                onCheckedChange = {
                    viewModel.autoAddNext = it
                },
                style = MaterialTheme.typography.labelLarge,
                size = 0.5f,
            )

            Box(modifier = Modifier.weight(1f)) {
                Text(
                    "${playlist.size}",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                TextButtonSmall(
                    { viewModel.addNextSmartTrack() },
                    "Add next",
                    Modifier.align(Alignment.CenterEnd),
                )
            }
        }

        mList.LazyColumn(playlist, { it.mediaId }, { item, closeDropdown ->
            if (mList.selectedItems.isEmpty())
                DropdownMenuItem(
                    text = { Text("Remove from playlist") },
                    onClick = {
                        viewModel.removeTrack(item)
                        closeDropdown()
                    })
            else
                DropdownMenuItem(
                    text = { Text("Remove selected from playlist") },
                    onClick = {
                        mList.selectedItems.forEach {
                            viewModel.removeTrack(it)
                        }
                        mList.selectedItems = listOf()
                        closeDropdown()
                    })
            if (mList.selectedItems.size <= 1)
                DropdownMenuItem(
                    text = { Text("Shuffle playlist") },
                    onClick = {
                        viewModel.shufflePlaylist()
                        closeDropdown()
                    })
            else
                DropdownMenuItem(
                    text = { Text("Shuffle selected") },
                    onClick = {
                        viewModel.shufflePlaylist(mList.selectedItems)
                        closeDropdown()
                    })
            DropdownMenuItem(
                text = { Text("Clear playlist") },
                onClick = {
                    viewModel.clearPlaylist()
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Move track up") },
                enabled = playlist.first() != item,
                onClick = {
                    viewModel.moveTrackUp(item)
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Move track down") },
                enabled = playlist.last() != item,
                onClick = {
                    viewModel.moveTrackDown(item)
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Move track to end") },
                enabled = playlist.last() != item,
                onClick = {
                    viewModel.moveTrackToEnd(item)
                    closeDropdown()
                })
        }, {
            if (it == currentTrack) CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) else null
        }, {
            viewModel.playTrack(it)
        }) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (it.file.author != "") "${it.file.author}: ${it.file.name}" else it.file.name,
                    modifier = Modifier.weight(1f)
                )
                Text(it.file.tagsLabel)
            }
        }
    }
}

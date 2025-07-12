package com.mixelte.melodorium.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.mixelte.melodorium.components.BetterLazyColumn
import com.mixelte.melodorium.components.SwitchWithLabel
import com.mixelte.melodorium.components.TextButtonSmall
import com.mixelte.melodorium.player.Player
import com.mixelte.melodorium.player.PlayerItem
import com.mixelte.melodorium.ui.theme.muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Playlist() {
    val mList = remember { BetterLazyColumn<PlayerItem>() }
    val clipboardManager = LocalClipboardManager.current

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
                    Player.current?.also {
                        Text(
                            if (it.file.author != "") "${it.file.author}: ${it.file.name}" else it.file.name,
                            modifier = Modifier.weight(1f),
                            minLines = 2,
                        )
                        Text(it.file.tagsLabel)
                    } ?: Text("No playing")
                }
                val rpath = Player.current?.file?.rpath ?: ""
                Text(
                    text = rpath,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(rpath))
                        },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.muted
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton({ Player.prevTrack() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev")
                }
                IconButton({ Player.playPause() }) {
                    if (Player.isPlaying) Icon(Icons.Filled.Pause, contentDescription = "Pause")
                    else Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                }
                IconButton({ Player.nextTrack() }) {
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
                checked = Player.autoAdd,
                onCheckedChange = {
                    Player.autoAdd = it
                },
                style = MaterialTheme.typography.labelLarge,
                size = 0.5f,
            )

            Box(modifier = Modifier.weight(1f)) {
                Text(
                    "${Player.playlist.size}",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                TextButtonSmall(
                    { Player.addNextTrack() },
                    "Add next",
                    Modifier.align(Alignment.CenterEnd),
                )
            }
        }

        mList.LazyColumn(Player.playlist, { it.mediaId }, { item, closeDropdown ->
            if (mList.selectedItems.isEmpty())
                DropdownMenuItem(
                    text = { Text("Remove from playlist") },
                    onClick = {
                        Player.removeTrack(item)
                        closeDropdown()
                    })
            else
                DropdownMenuItem(
                    text = { Text("Remove selected from playlist") },
                    onClick = {
                        mList.selectedItems.forEach {
                            Player.removeTrack(it)
                        }
                        mList.selectedItems = listOf()
                        closeDropdown()
                    })
            if (mList.selectedItems.size <= 1)
                DropdownMenuItem(
                    text = { Text("Shuffle playlist") },
                    onClick = {
                        Player.shuffle()
                        closeDropdown()
                    })
            else
                DropdownMenuItem(
                    text = { Text("Shuffle selected") },
                    onClick = {
                        Player.shuffle(mList.selectedItems)
                        closeDropdown()
                    })
            DropdownMenuItem(
                text = { Text("Clear playlist") },
                onClick = {
                    Player.clear()
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Move track up") },
                enabled = Player.playlist.first() != item,
                onClick = {
                    val i = Player.playlist.indexOf(item)
                    Player.moveTrack(i, i - 1)
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Move track down") },
                enabled = Player.playlist.last() != item,
                onClick = {
                    val i = Player.playlist.indexOf(item)
                    Player.moveTrack(i, i + 1)
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Move track to end") },
                enabled = Player.playlist.last() != item,
                onClick = {
                    Player.moveTrack(item, Player.playlist.size - 1)
                    closeDropdown()
                })
        }, {
            if (it == Player.current) CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) else null
        }, {
            Player.play(it)
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

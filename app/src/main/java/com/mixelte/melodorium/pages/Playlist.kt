package com.mixelte.melodorium.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.components.BetterLazyColumn
import com.mixelte.melodorium.data.MusicFile
import com.mixelte.melodorium.player.Player
import com.mixelte.melodorium.ui.theme.muted

@Composable
fun Playlist() {
    val mList = remember { BetterLazyColumn<MusicFile>() }
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
                            if (it.author != "") "${it.author}: ${it.name}" else it.name,
                            modifier = Modifier.weight(1f),
                            minLines = 2,
                        )
                        Text(it.tagsLabel)
                    } ?: Text("No playing")
                }
                val rpath = Player.current?.rpath ?: ""
                Text(
                    text = rpath,
                    modifier = Modifier.padding(start = 5.dp).clickable {
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
        mList.LazyColumn(Player.playlist, { it.rpath }, { file, closeDropdown ->
            DropdownMenuItem(
                text = { Text("Remove from playlist") },
                onClick = {
                    Player.removeTrack(file)
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Remove selected from playlist") },
                enabled = mList.selectedItems.isNotEmpty(),
                onClick = {
                    mList.selectedItems.forEach {
                        Player.removeTrack(it)
                    }
                    mList.selectedItems = listOf()
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Shuffle playlist") },
                onClick = {
                    Player.shuffle()
                    closeDropdown()
                })
            DropdownMenuItem(
                text = { Text("Shuffle selected") },
                enabled = mList.selectedItems.size > 1,
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
                Text(if (it.author != "") "${it.author}: ${it.name}" else it.name, modifier = Modifier.weight(1f))
                Text(it.tagsLabel)
            }
        }
    }
}

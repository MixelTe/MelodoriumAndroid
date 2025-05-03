package com.mixelte.melodorium

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.ui.theme.muted


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun MusicList() {
    var mList = remember { BetterLazyColumn<MusicFile>() }

    Column(
        modifier = Modifier.padding(5.dp)
    ) {
        if (MusicData.IsLoading)
            return Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text("Loading files")
            }

        MusicDataFilter.Updater()
        ExpandableBox({ (if (it) "" else MusicDataFilter.title).ifBlank { "Filter" } }) {
            OutlinedTextField(
                MusicDataFilter.author,
                { MusicDataFilter.author = it },
                label = { Text("Author") },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                MusicDataFilter.name,
                { MusicDataFilter.name = it },
                label = { Text("Name") },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            @Composable
            fun <T> FilterDropdown(all: List<T>, cur: MutableList<T>, name: String) {
                SelectableDropdownMenu(
                    all,
                    { it in cur },
                    { it, selected -> if (selected) cur.add(it) else cur.remove(it) },
                    { it.toString() }) {
                    Text(
                        when (cur.size) {
                            all.size, 0 -> "All $name"
                            else -> cur.joinToString(", ")
                        }
                    )
                }
            }
            FilterDropdown(MusicMood.entries, MusicDataFilter.mood, "mood")
            FilterDropdown(MusicLike.entries, MusicDataFilter.like, "like")
            FilterDropdown(MusicLang.entries, MusicDataFilter.lang, "lang")
            FilterDropdown(MusicEmo.entries, MusicDataFilter.emo, "emo")
            FilterDropdown(MusicData.Tags, MusicDataFilter.tags, "tags")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val allSelected = mList.selectedItems.size == MusicDataFilter.files.size
            TextButton({
                mList.selectedItems = if (allSelected) listOf() else MusicDataFilter.files
            }) { Text(if (allSelected) "Unselect all" else "Select all") }
            Text(
                if (mList.selectedItems.isEmpty()) "${MusicDataFilter.files.size}"
                else "${mList.selectedItems.size}/${MusicDataFilter.files.size}",
                style = MaterialTheme.typography.labelSmall,
            )
            TextButton({ MusicDataFilter.reset() }) { Text("Reset filter") }
        }
        MusicData.Error?.let { Text(it) }
        mList.LazyColumn(
            items = MusicDataFilter.files,
            itemKey = { it.rpath },
            dropdownItems = { file, closeDropdown ->
                DropdownMenuItem(
                    text = { Text("Add to playlist") },
                    onClick = {
                        Player.addTrack(file)
                        closeDropdown()
                    })
                DropdownMenuItem(
                    text = { Text("Add selected to playlist") },
                    enabled = mList.selectedItems.isNotEmpty(),
                    onClick = {
                        mList.selectedItems.forEach {
                            Player.addTrack(it)
                        }
                        mList.selectedItems = listOf()
                        closeDropdown()
                    })
            },
            colors = null
        ) { file ->
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        if (file.author != "") "${file.author}: ${file.name}" else file.name,
                        modifier = Modifier.weight(1f)
                    )
                    Text(file.tagsLabel)
                }
                Text(
                    text = file.rpath,
                    modifier = Modifier.padding(start = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.muted
                )
            }
        }
    }
}

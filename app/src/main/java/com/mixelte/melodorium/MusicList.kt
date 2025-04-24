package com.mixelte.melodorium

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.ui.theme.muted
import kotlin.enums.EnumEntries
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MusicList() {
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    var selectedItems by remember { mutableStateOf(listOf<MusicFile>()) }
    var lastCheckTime by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
    var lastCheckItem by remember { mutableStateOf(Pair<MusicFile?, MusicFile?>(null, null)) }
    val scope = rememberCoroutineScope()
    val filter = remember { MusicDataFilter(scope) }

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
        OutlinedTextField(
            filter.author,
            { filter.author = it },
            label = { Text("Author") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            filter.name,
            { filter.name = it },
            label = { Text("Name") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        @Composable
        fun <T> FilterDropdown(all: List<T>, cur: MutableList<T>, name: String) {
            SelectableDropdownMenu(all,
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
        FilterDropdown(MusicMood.entries, filter.mood, "mood")
        FilterDropdown(MusicLike.entries, filter.like, "like")
        FilterDropdown(MusicLang.entries, filter.lang, "lang")
        FilterDropdown(MusicEmo.entries, filter.emo, "emo")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val allSelected = selectedItems.size == filter.files.size
            TextButton({
                selectedItems = if (allSelected) listOf() else filter.files
            }) { Text(if (allSelected) "Unselect all" else "Select all") }
            TextButton({ filter.reset() }) { Text("Reset filter") }
        }
        MusicData.Error?.let { Text(it) }
        LazyColumn {
            items(filter.files) { file ->
                var offset by remember { mutableStateOf(Offset.Zero) }
                var dropDownExpanded by remember { mutableStateOf(false) }
                val (xDp, yDp) = with(density) {
                    (offset.x.toDp()) to (offset.y.toDp())
                }
                val selected = file in selectedItems
                Row {
                    Checkbox(
                        selected,
                        {
                            val mark = TimeSource.Monotonic.markNow()
                            val prevMark = lastCheckTime
                            val (prevItem2, prevItem1) = lastCheckItem
                            lastCheckTime = mark
                            lastCheckItem = prevItem1 to file
                            if (prevItem2 != null && prevItem2 != file && prevItem1 == file
                                && prevMark?.let { mark - it < 300.milliseconds } == true
                            ) {
                                val anotherI = MusicData.Files.indexOf(prevItem2)
                                val thisI = MusicData.Files.indexOf(file)
                                val list = selectedItems.toMutableList()
                                val select = prevItem2 in selectedItems
                                for (i in min(thisI, anotherI)..max(thisI, anotherI)) {
                                    val item = MusicData.Files[i];
                                    if (select) {
                                        if (item !in list)
                                            list.add(item)
                                    } else {
                                        if (item in list)
                                            list.remove(item)
                                    }
                                }
                                selectedItems = list;
                            } else {
                                selectedItems =
                                    if (selected) selectedItems.toMutableList()
                                        .also { it.remove(file) }
                                    else selectedItems.toMutableList().also { it.add(file) }
                            }
                        },
                    )
                    Column(
                        modifier = Modifier
                            .pointerInteropFilter {
                                offset = Offset(it.x, it.y)
                                false
                            }
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dropDownExpanded = true
                                })
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                if (file.author != "") "${file.author}: ${file.name}" else file.name,
                                modifier = Modifier.weight(1f)
                            )
                            Text(file.tags)
                        }
                        Text(
                            text = file.rpath,
                            modifier = Modifier.padding(start = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.muted
                        )
                        DropdownMenu(
                            expanded = dropDownExpanded,
                            offset = DpOffset(xDp, yDp),
                            onDismissRequest = { dropDownExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Add to playlist") },
                                onClick = {
                                    PlaylistData.addTrack(file)
                                    dropDownExpanded = false
                                })
                            DropdownMenuItem(
                                text = { Text("Add selected to playlist") },
                                enabled = selectedItems.isNotEmpty(),
                                onClick = {
                                    selectedItems.forEach {
                                        PlaylistData.addTrack(it)
                                    }
                                    selectedItems = mutableListOf()
                                    dropDownExpanded = false
                                })
                        }
                    }
                }
            }
        }
    }
}

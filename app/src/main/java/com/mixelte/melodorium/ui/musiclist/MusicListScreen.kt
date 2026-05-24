package com.mixelte.melodorium.ui.musiclist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mixelte.melodorium.domain.models.MusicEmo
import com.mixelte.melodorium.domain.models.MusicFile
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.domain.models.MusicPublic
import com.mixelte.melodorium.ui.components.BetterLazyColumn
import com.mixelte.melodorium.ui.components.ExpandableBox
import com.mixelte.melodorium.ui.components.SelectableDropdownMenu
import com.mixelte.melodorium.ui.components.TextFieldWithHints
import com.mixelte.melodorium.ui.theme.Tomato
import com.mixelte.melodorium.ui.theme.muted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MusicListScreen(viewModel: MusicListViewModel) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val files by viewModel.files.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val mList = remember { BetterLazyColumn<MusicFile>() }

    Column(modifier = Modifier.padding(5.dp)) {
        if (isLoading || files.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text("Loading files")
                } else {
                    Text(text = error ?: "Select folder in settings", color = Color.Tomato)
                }
            }
            return
        }

        ExpandableBox({ (if (it) "" else title).ifBlank { "Filter" } }) {
            TextFieldWithHints(
                value = filters.authorQuery,
                onValueChange = viewModel::onAuthorQueryChanged,
                options = folders,
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = filters.nameQuery,
                onValueChange = viewModel::onNameQueryChanged,
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            @Composable
            fun <T> FilterDropdown(all: List<T>, cur: List<T>, toggle: (T, Boolean) -> Unit, name: String) {
                SelectableDropdownMenu(
                    items = all,
                    isSelected = { it in cur },
                    setSelected = toggle,
                    itemName = { it.toString() }) {
                    Text(
                        when (cur.size) {
                            all.size, 0 -> "All $name"
                            else -> cur.joinToString(", ")
                        }
                    )
                }
            }
            FilterDropdown(MusicMood.entries, filters.selectedMoods, viewModel::onMoodToggled, "mood")
            FilterDropdown(MusicLike.entries, filters.selectedLikes, viewModel::onLikeToggled, "like")
            FilterDropdown(MusicLang.entries, filters.selectedLangs, viewModel::onLangToggled, "lang")
            FilterDropdown(MusicEmo.entries, filters.selectedEmos, viewModel::onEmoToggled, "emo")
            FilterDropdown(tags, filters.selectedTags, viewModel::onTagToggled, "tags")
            FilterDropdown(folders, filters.selectedFolders, viewModel::onFolderToggled, "folders")
            FilterDropdown(MusicPublic.entries, filters.selectedPublics, viewModel::onPublicToggled, "public")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val noSelected = mList.selectedItems.isEmpty()
            TextButton({
                mList.selectedItems = if (noSelected) files else listOf()
            }) { Text(if (noSelected) "Select all" else "Unselect all") }
            Text(
                if (noSelected) "${files.size}"
                else "${mList.selectedItems.size}/${files.size}",
                style = MaterialTheme.typography.labelSmall,
            )
            TextButton({ viewModel.onResetFilters() }) { Text("Reset filter") }
        }
        error?.let { Text(it, color = Color.Tomato) }
        val refreshScope = rememberCoroutineScope()
        var refreshing by remember { mutableStateOf(false) }
        val state = rememberPullRefreshState(
            refreshing,
            { refreshScope.launch { viewModel.updateFiles() } }
        )
        Box(Modifier.pullRefresh(state)) {
            mList.LazyColumn(
                items = files,
                itemKey = { it.rpath },
                dropdownItems = { file, closeDropdown ->
                    if (mList.selectedItems.isEmpty())
                        DropdownMenuItem(
                            text = { Text("Add to playlist") },
                            onClick = {
                                viewModel.addTrackToPlaylist(file)
                                closeDropdown()
                            })
                    else
                        DropdownMenuItem(
                            text = { Text("Add selected to playlist") },
                            onClick = {
                                viewModel.addTracksToPlaylist(mList.selectedItems)
                                mList.selectedItems = listOf()
                                closeDropdown()
                            })
                    DropdownMenuItem(
                        text = { Text("Add selected to playlist randomly") },
                        enabled = mList.selectedItems.isNotEmpty(),
                        onClick = {
                            viewModel.addTracksToPlaylist(mList.selectedItems.shuffled())
                            mList.selectedItems = listOf()
                            closeDropdown()
                        })
                },
                colors = null,
                {}
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
            PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
        }
    }
}
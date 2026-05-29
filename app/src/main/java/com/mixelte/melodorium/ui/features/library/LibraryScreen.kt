package com.mixelte.melodorium.ui.features.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.ui.common.EmptyState
import com.mixelte.melodorium.ui.common.TrackList
import com.mixelte.melodorium.ui.common.UiTrack

@Composable
fun LibraryRoute(viewModel: LibraryViewModel) {
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()

    LibraryScreen(
        isSearching,
        query,
        { viewModel.setQuery(it) },
        { viewModel.cancelSearching() },
        tracks
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    isSearching: Boolean,
    query: String,
    onQueryChange: (query: String) -> Unit,
    onCancelSearching: () -> Unit,
    tracks: List<UiTrack>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = { },
                    enabled = true,
                    placeholder = { Text("Поиск") },
                    leadingIcon = {
                        if (isSearching) {
                            IconButton(onClick = onCancelSearching) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск")
                        }
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    },
                )
            },
            expanded = false,
            onExpandedChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = {},
        )

        if (tracks.isEmpty()) {
            if (query.isBlank()) {
                EmptyState(
                    title = "Нет файлов",
                    subtitle = "Библиотека пока пуста",
                )
            } else {
                EmptyState(
                    title = "Нет результатов",
                    subtitle = "По вашему запросу ничего не найдено",
                )
            }
        } else {
            TrackList(tracks)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    LibraryScreen(
        false,
        "",
        {},
        {},
        listOf(
            UiTrack(
                id = 1,
                title = "Трава у дома",
                artist = "Земляне",
                mood = MusicMood.Energistic,
                like = MusicLike.Like,
                lang = MusicLang.Ru,
            ),
            UiTrack(
                id = 2,
                title = "In The Night",
                artist = "Pet Shop Boys",
                mood = MusicMood.Energistic,
                like = MusicLike.Like,
                lang = MusicLang.En,
            ),
            UiTrack(
                id = 3,
                title = "Костёр",
                artist = "Машина времени",
                mood = MusicMood.Calm,
                like = MusicLike.Best,
                lang = MusicLang.Ru,
            ),
        ),
    )
}
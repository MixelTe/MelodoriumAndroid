package com.mixelte.melodorium.ui.features.wave_settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.R
import com.mixelte.melodorium.ui.features.wave_settings.ArtistSearchState
import com.mixelte.melodorium.ui.features.wave_settings.WaveSettingsUiChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectArtistsView(
    artistSearchState: ArtistSearchState,
    onSearchQueryChanged: (String) -> Unit,
    artists: List<WaveSettingsUiChip>,
    clearArtist: () -> Unit,
    onArtistToggled: (artist: String, isSelected: Boolean?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Исполнитель",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = clearArtist,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_remove),
                    contentDescription = "Очистить секцию",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(artists.filter { it.isSelected }) { artist ->
                InputChip(
                    selected = true,
                    onClick = { onArtistToggled(artist.id, false) },
                    label = { Text(artist.text) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Удалить",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }

        DockedSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            inputField = {
                SearchBarDefaults.InputField(
                    query = artistSearchState.query,
                    onQueryChange = onSearchQueryChanged,
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = { },
                    enabled = true,
                    placeholder = { Text("Поиск") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск")
                    },
                    trailingIcon = {
                        if (artistSearchState.query.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    },
                )
            },
            expanded = true,
            onExpandedChange = { },
            content = {
                LazyColumn {
                    items(artistSearchState.suggestions) { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onArtistToggled(suggestion, true)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                }
            },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SelectArtistsViewPreview() {
    val artists = listOf("Машина времени", "Король и Шут", "Pet Shop Boys")
    SelectArtistsView(
        artistSearchState = ArtistSearchState(
            query = "Земляне",
            suggestions = listOf("Земляне") + artists,
        ),
        onSearchQueryChanged = { },
        artists = artists.map { WaveSettingsUiChip(it, it, true) },
        clearArtist = { },
        onArtistToggled = { _, _ -> },
    )
}

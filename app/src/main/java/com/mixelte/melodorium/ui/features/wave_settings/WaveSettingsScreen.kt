package com.mixelte.melodorium.ui.features.wave_settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mixelte.melodorium.R
import com.mixelte.melodorium.ui.common.TrackList
import com.mixelte.melodorium.ui.features.wave_settings.components.ChipsRow
import com.mixelte.melodorium.ui.features.wave_settings.components.SelectArtistsView
import com.mixelte.melodorium.ui.features.wave_settings.components.SettingsSection
import com.mixelte.melodorium.ui.theme.Tomato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveSettingsRoute(viewModel: WaveSettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val artistSearchState by viewModel.artistSearchState.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    var showPlaylist by rememberSaveable { mutableStateOf(false) }
    var showSelectArtists by rememberSaveable { mutableStateOf(false) }

    fun toggleSection(value: Boolean, items: List<WaveSettingsUiChip>, toggle: (String, Boolean?) -> Unit) {
        items.forEach { toggle(it.id, value) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WaveSettingsScreen(
            state = uiState,
            openMenu = {},
            clearFilters = { viewModel.clearFilters() },
            openList = { showPlaylist = true },
            openSelectArtists = { showSelectArtists = true },
            toggleArtist = viewModel::onArtistToggled,
            toggleMood = viewModel::onMoodToggled,
            toggleLike = viewModel::onLikeToggled,
            toggleLang = viewModel::onLangToggled,
            toggleTag = viewModel::onTagToggled,
            toggleOther = viewModel::onOtherToggled,
            clearArtist = { toggleSection(false, uiState.artists, viewModel::onArtistToggled) },
            clearMood = { toggleSection(false, uiState.moods, viewModel::onMoodToggled) },
            clearLike = { toggleSection(false, uiState.likes, viewModel::onLikeToggled) },
            clearLang = { toggleSection(false, uiState.langs, viewModel::onLangToggled) },
            clearTag = { toggleSection(false, uiState.tags, viewModel::onTagToggled) },
            clearOther = { toggleSection(false, uiState.tags, viewModel::onOtherToggled) },
            selectAllArtist = { toggleSection(true, uiState.artists, viewModel::onArtistToggled) },
            selectAllMood = { toggleSection(true, uiState.moods, viewModel::onMoodToggled) },
            selectAllLike = { toggleSection(true, uiState.likes, viewModel::onLikeToggled) },
            selectAllLang = { toggleSection(true, uiState.langs, viewModel::onLangToggled) },
            selectAllTag = { toggleSection(true, uiState.tags, viewModel::onTagToggled) },
            selectAllOther = { toggleSection(true, uiState.tags, viewModel::onOtherToggled) },
        )

        if (showPlaylist) {
            ModalBottomSheet(
                onDismissRequest = { showPlaylist = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                TrackList(filteredTracks)
            }
        }

        if (showSelectArtists) {
            ModalBottomSheet(
                onDismissRequest = { showSelectArtists = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                SelectArtistsView(
                    artistSearchState = artistSearchState,
                    onSearchQueryChanged = viewModel::onArtistSearchQueryChanged,
                    artists = uiState.artists,
                    clearArtist = { toggleSection(false, uiState.artists, viewModel::onArtistToggled) },
                    onArtistToggled = viewModel::onArtistToggled
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveSettingsScreen(
    state: WaveSettingsUiState,
    openMenu: () -> Unit,
    clearFilters: () -> Unit,
    openList: () -> Unit,
    openSelectArtists: () -> Unit,
    toggleArtist: (String, Boolean?) -> Unit,
    toggleMood: (String) -> Unit,
    toggleLike: (String) -> Unit,
    toggleLang: (String) -> Unit,
    toggleTag: (String) -> Unit,
    toggleOther: (String) -> Unit,
    clearArtist: () -> Unit,
    clearMood: () -> Unit,
    clearLike: () -> Unit,
    clearLang: () -> Unit,
    clearTag: () -> Unit,
    clearOther: () -> Unit,
    selectAllArtist: () -> Unit,
    selectAllMood: () -> Unit,
    selectAllLike: () -> Unit,
    selectAllLang: () -> Unit,
    selectAllTag: () -> Unit,
    selectAllOther: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = openMenu) {
                Icon(Icons.Default.Menu, contentDescription = "Меню")
            }

            Text(
                text = "Настройки волны",
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineLarge,
            )

            IconButton(onClick = clearFilters) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_broom),
                    contentDescription = "Сбросить"
                )
            }
        }
        if (!state.error.isNullOrEmpty()) {
            Text(
                text = state.error,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Tomato,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally),
            )
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSection(title = "Темп", onClearClick = clearMood, onSelectAllClick = selectAllMood) {
                ChipsRow(items = state.moods, toggle = toggleMood)
            }

            SettingsSection(title = "Степень любви", onClearClick = clearLike, onSelectAllClick = selectAllLike) {
                ChipsRow(items = state.likes, toggle = toggleLike)
            }

            SettingsSection(title = "Язык", onClearClick = clearLang, onSelectAllClick = selectAllLang) {
                ChipsRow(items = state.langs, toggle = toggleLang, wrap = true)
            }

            val selectedTags = state.tags.filter { it.isSelected }
            SettingsSection(
                title = "Категория" + if (selectedTags.isEmpty()) "" else " (${selectedTags.size}/${state.tags.size})",
                onClearClick = clearTag, onSelectAllClick = selectAllTag
            ) {
                ChipsRow(items = state.tags, toggle = toggleTag)
            }

            val selectedArtists = state.artists.filter { it.isSelected }
            SettingsSection(
                title = "Исполнитель" + if (selectedArtists.isEmpty()) "" else " (${selectedArtists.size}/${state.artists.size})",
                onClearClick = clearArtist, onSelectAllClick = selectAllArtist
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = openSelectArtists) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = "Добавить",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedArtists.forEach { artist ->
                            InputChip(
                                selected = true,
                                onClick = { toggleArtist(artist.text, false) },
                                label = { Text(artist.text) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Удалить",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "Атмосфера", onClearClick = clearOther, onSelectAllClick = selectAllOther) {
                ChipsRow(items = state.other, toggle = toggleOther, wrap = true)
            }

            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text("Loading files", style = MaterialTheme.typography.headlineSmall)
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = openList,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Треки в волне: ${state.trackCount}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF8F8)
@Composable
fun SettingsScreenPreview() {
    val moods = listOf("Сон", "Спокойное", "Бодрое", "Энергичное")
    val likes = listOf("Избранное", "Любимое", "Приятное")
    val langs = listOf("🇷🇺 Ру", "🇬🇧 En", "🇫🇷 Fr", "🇩🇪 De", "🇮🇹 It")
    val artists = listOf("Машина времени", "Король и Шут", "Pet Shop Boys")
    val tags = listOf("Джаз", "Новогоднее", "Забавное", "Электроника", "Необычное")
    val other = listOf("\uD83E\uDD73", "\uD83D\uDE0C", "\uD83D\uDE22", "\uD83D\uDC64", "\uD83D\uDC65")
    val selectedMoods = listOf("Спокойное", "Бодрое")
    val selectedLikes = listOf("Избранное")
    val selectedLangs = listOf("🇷🇺 Ру", "🇬🇧 En", "🇫🇷 Fr")
    val selectedArtists = listOf("Машина времени", "Король и Шут", "Pet Shop Boys")
    val selectedTags = listOf("Джаз", "Новогоднее", "Забавное")

    WaveSettingsScreen(
        WaveSettingsUiState(
            moods = moods.map { WaveSettingsUiChip(it, it, it in selectedMoods) },
            likes = likes.map { WaveSettingsUiChip(it, it, it in selectedLikes) },
            langs = langs.map { WaveSettingsUiChip(it, it, it in selectedLangs) },
            artists = artists.map { WaveSettingsUiChip(it, it, it in selectedArtists) },
            tags = tags.map { WaveSettingsUiChip(it, it, it in selectedTags) },
            other = other.map { WaveSettingsUiChip(it, it, false) },
            trackCount = 435,
            isLoading = false,
//            error = "123",
        ), {}, {}, {}, {}, { _, _ -> }, { }, { }, { }, { }, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
    )
}

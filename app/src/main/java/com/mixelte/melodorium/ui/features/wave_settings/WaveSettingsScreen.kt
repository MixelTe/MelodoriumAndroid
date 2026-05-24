package com.mixelte.melodorium.ui.features.wave_settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mixelte.melodorium.R
import com.mixelte.melodorium.ui.features.wave_settings.components.ChipsRow
import com.mixelte.melodorium.ui.features.wave_settings.components.SettingsSection
import com.mixelte.melodorium.ui.theme.Tomato

@Composable
fun WaveSettingsRoute(viewModel: WaveSettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun clearSection(items: List<WaveSettingsUiChip>, toggle: (String, Boolean?) -> Unit) {
        items.forEach { toggle(it.id, false) }
    }
    WaveSettingsScreen(
        state = uiState,
        openMenu = {},
        clearFilters = { viewModel.clearFilters() },
        openList = {},
        toggleAuthor = viewModel::onAuthorToggled,
        toggleMood = viewModel::onMoodToggled,
        toggleLike = viewModel::onLikeToggled,
        toggleLang = viewModel::onLangToggled,
        toggleTag = viewModel::onTagToggled,
        clearAuthor = { clearSection(uiState.authors, viewModel::onAuthorToggled) },
        clearMood = { clearSection(uiState.moods, viewModel::onMoodToggled) },
        clearLike = { clearSection(uiState.likes, viewModel::onLikeToggled) },
        clearLang = { clearSection(uiState.langs, viewModel::onLangToggled) },
        clearTag = { clearSection(uiState.tags, viewModel::onTagToggled) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveSettingsScreen(
    state: WaveSettingsUiState,
    openMenu: () -> Unit,
    clearFilters: () -> Unit,
    openList: () -> Unit,
    toggleAuthor: (String, Boolean?) -> Unit,
    toggleMood: (String) -> Unit,
    toggleLike: (String) -> Unit,
    toggleLang: (String) -> Unit,
    toggleTag: (String) -> Unit,
    clearAuthor: () -> Unit,
    clearMood: () -> Unit,
    clearLike: () -> Unit,
    clearLang: () -> Unit,
    clearTag: () -> Unit,
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
            SettingsSection(title = "Темп", onClearClick = clearMood) {
                ChipsRow(items = state.moods, toggle = toggleMood)
            }

            SettingsSection(title = "Степень любви", onClearClick = clearLike) {
                ChipsRow(items = state.likes, toggle = toggleLike)
            }

            SettingsSection(title = "Язык", onClearClick = clearLang) {
                ChipsRow(items = state.langs, toggle = toggleLang, wrap = true)
            }

            SettingsSection(title = "Категория", onClearClick = clearTag) {
                ChipsRow(items = state.tags, toggle = toggleTag)
            }

            SettingsSection(title = "Исполнитель", onClearClick = clearAuthor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO */ }) {
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
                        state.authors.filter { it.isSelected }.forEach { author ->
                            InputChip(
                                selected = true,
                                onClick = { toggleAuthor(author.text, false) },
                                label = { Text(author.text) },
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
                Spacer(modifier = Modifier.height(16.dp))
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
    val artists = listOf("Земфира", "Машина времени", "Король и Шут")
    val tags = listOf("Джаз", "Новогоднее", "Забавное", "Электроника", "Необычное")
    val selectedMoods = listOf("Спокойное", "Бодрое")
    val selectedLikes = listOf("Избранное")
    val selectedLangs = listOf("🇷🇺 Ру", "🇬🇧 En", "🇫🇷 Fr")
    val selectedArtists = listOf("Земфира", "Машина времени", "Король и Шут")
    val selectedTags = listOf("Джаз", "Новогоднее", "Забавное")

    WaveSettingsScreen(
        WaveSettingsUiState(
            moods = moods.map { WaveSettingsUiChip(it, it, it in selectedMoods) },
            likes = likes.map { WaveSettingsUiChip(it, it, it in selectedLikes) },
            langs = langs.map { WaveSettingsUiChip(it, it, it in selectedLangs) },
            authors = artists.map { WaveSettingsUiChip(it, it, it in selectedArtists) },
            tags = tags.map { WaveSettingsUiChip(it, it, it in selectedTags) },
            trackCount = 435,
            isLoading = false,
//            error = "123",
        ), {}, {}, {}, { _, _ -> }, { }, { }, { }, { }, {}, {}, {}, {}, {}
    )
}

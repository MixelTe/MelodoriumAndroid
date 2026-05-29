package com.mixelte.melodorium.ui.features.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.ui.common.TrackListItem
import com.mixelte.melodorium.ui.common.UiTrack

@Composable
fun PlaylistScreen(
    tracks: List<UiTrack>,
    onBackClick: () -> Unit,
    onClearQueueClick: () -> Unit,
    onTrackClick: (UiTrack) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Назад",
                )
            }

            Text(
                text = "Очередь воспроизведения",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
            )

            IconButton(onClick = onClearQueueClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Очистить очередь",
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    onClick = { onTrackClick(track) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistPreview() {
    PlaylistScreen(
        listOf(
            UiTrack(
                id = 1,
                title = "Трава у дома",
                artist = "Земляне",
                mood = MusicMood.Energistic,
                like = MusicLike.Like,
                lang = MusicLang.Ru,
                isPlaying = true,
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
        {},
        {},
        {},
    )
}
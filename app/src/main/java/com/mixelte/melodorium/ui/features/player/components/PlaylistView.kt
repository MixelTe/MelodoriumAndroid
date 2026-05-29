package com.mixelte.melodorium.ui.features.player.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
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
import com.mixelte.melodorium.domain.models.MusicEmo
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.domain.models.MusicPublic
import com.mixelte.melodorium.ui.common.TrackList
import com.mixelte.melodorium.ui.common.TrackMenuAction
import com.mixelte.melodorium.ui.common.UiTrack

@Composable
fun PlaylistView(
    tracks: List<UiTrack>,
    onBackClick: () -> Unit,
    onClearQueueClick: () -> Unit,
    onTrackClick: (UiTrack) -> Unit,
    onTrackRemove: (UiTrack) -> Unit,
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

        TrackList(
            tracks,
            modifier = Modifier.weight(1f),
            onTrackClick = { onTrackClick(it) },
            getMenuActions = { track ->
                listOf(
                    TrackMenuAction(
                        text = "Удалить из очереди",
                        icon = Icons.Default.Delete,
                        onClick = { onTrackRemove(track) }
                    )
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistPreview() {
    PlaylistView(
        listOf(
            UiTrack(
                id = 1,
                title = "Трава у дома",
                artist = "Земляне",
                mood = MusicMood.Energistic,
                like = MusicLike.Like,
                lang = MusicLang.Ru,
                emo = MusicEmo.Neutral,
                public = MusicPublic.Public,
                isPlaying = true,
            ),
            UiTrack(
                id = 2,
                title = "In The Night",
                artist = "Pet Shop Boys",
                mood = MusicMood.Energistic,
                like = MusicLike.Like,
                lang = MusicLang.En,
                emo = MusicEmo.Neutral,
                public = MusicPublic.Public,
            ),
            UiTrack(
                id = 3,
                title = "Костёр",
                artist = "Машина времени",
                mood = MusicMood.Calm,
                like = MusicLike.Best,
                lang = MusicLang.Ru,
                emo = MusicEmo.Neutral,
                public = MusicPublic.Public,
            ),
        ),
        {},
        {},
        {},
        {},
    )
}
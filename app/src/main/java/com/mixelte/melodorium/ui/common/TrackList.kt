package com.mixelte.melodorium.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mixelte.melodorium.domain.models.MusicEmo
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.domain.models.MusicPublic


@Composable
fun TrackList(
    tracks: List<UiTrack>,
    modifier: Modifier = Modifier,
    onTrackClick: ((UiTrack) -> Unit)? = null,
    getMenuActions: (UiTrack) -> List<TrackMenuAction> = { emptyList() },
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(tracks, key = { it.id }) { track ->
            TrackListItem(
                track = track,
                onClick = onTrackClick?.let { { it(track) } },
                menuActions = getMenuActions(track),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackListPreview() {
    TrackList(
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
    )
}
package com.mixelte.melodorium.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood


@Composable
fun TrackList(
    tracks: List<UiTrack>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(tracks, key = { it.id }) { track ->
            TrackListItem(track)
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
    )
}
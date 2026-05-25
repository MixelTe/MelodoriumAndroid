package com.mixelte.melodorium.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.ui.features.player.PlayerUiTrack

@Composable
fun TrackListItem(
    track: PlayerUiTrack,
    onClick: () -> Unit
) {
    val backgroundColor = if (track.isPlaying) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    ListItem(
        modifier = Modifier.background(backgroundColor).clickable {
            onClick()
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),

        leadingContent = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                TrackArtwork(track.artwork, size = 56.dp)
            }
        },

        headlineContent = {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        supportingContent = {
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },

        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicMoodBadge(track.mood, track.like, track.lang)

                IconButton(onClick = { /* Действие */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Еще"
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TrackListItemPreview() {
    TrackListItem(
        PlayerUiTrack(
            id = "1",
            title = "Трава у дома",
            artist = "Земляне",
            mood = MusicMood.Energistic,
            like = MusicLike.Like,
            lang = MusicLang.Ru,
        ),
        {},
    )
}
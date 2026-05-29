package com.mixelte.melodorium.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.ui.theme.getBackgroundColor
import com.mixelte.melodorium.ui.theme.getStarColor

@Composable
fun MusicMoodBadge(
    mood: MusicMood,
    like: MusicLike,
    lang: MusicLang,
    modifier: Modifier = Modifier
) {
    val starCount = when (like) {
        MusicLike.Normal -> 0
        MusicLike.Good -> 1
        MusicLike.Like -> 2
        MusicLike.Best -> 3
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(mood.getBackgroundColor()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = lang.toName(),
            fontSize = 14.sp,
            modifier = Modifier
                .offset(y = (if (starCount > 0) -2 else 0).dp)
        )

        if (starCount == 1 || starCount == 3) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = mood.getStarColor(),
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (if (starCount == 3) 1 else 0).dp)
            )
        }
        if (starCount >= 2) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = mood.getStarColor(),
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.BottomCenter)
                    .offset(x = (if (starCount == 2) -5 else -8).dp, y = (-1).dp)
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = mood.getStarColor(),
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.BottomCenter)
                    .offset(x = (if (starCount == 2) 5 else 8).dp, y = (-1).dp)
            )
        }

//
//        if (starCount > 0) {
//            Row(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 2.dp),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                for (i in 0 until starCount) {
//                    val yOffset = if (starCount == 3 && (i == 0 || i == 2)) (-2).dp else 0.dp
//
//                    Icon(
//                        imageVector = Icons.Filled.Star,
//                        contentDescription = null,
//                        tint = mood.getStarColor(),
//                        modifier = Modifier
//                            .size(14.dp)
//                            .offset(y = yOffset)
//                    )
//                }
//            }
//        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF8F8)
@Composable
fun MusicMoodGridPreview() {
    val moods = MusicMood.entries.toTypedArray()
    val likes = arrayOf(MusicLike.Normal, MusicLike.Good, MusicLike.Like, MusicLike.Best)
    var currentLang = 0

    LazyVerticalGrid(
        modifier = Modifier.width(250.dp),
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        moods.forEach { mood ->
            likes.forEach { like ->
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        var i = currentLang++ % (MusicLang.entries.size - 1) + 1
                        if (i == 2) i = 1
                        MusicMoodBadge(
                            mood = mood,
                            like = like,
                            lang = MusicLang.entries[i]
                        )
                    }
                }
            }
        }
    }
}
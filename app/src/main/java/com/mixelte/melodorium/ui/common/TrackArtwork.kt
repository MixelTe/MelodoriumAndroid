package com.mixelte.melodorium.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mixelte.melodorium.R

@Composable
fun TrackArtwork(
    artworkModel: Any?,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    borderRadius: Dp = 8.dp,
) {
    AsyncImage(
        model = artworkModel,
        contentDescription = "Обложка трека",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(borderRadius)),
        contentScale = ContentScale.Crop,

        placeholder = painterResource(R.drawable.ic_default_track_cover),
        error = painterResource(R.drawable.ic_default_track_cover),
//        onError = { state ->
//            println("Coil Error: ${state.result.throwable}\nImage:$artworkModel")
//        }
    )
}
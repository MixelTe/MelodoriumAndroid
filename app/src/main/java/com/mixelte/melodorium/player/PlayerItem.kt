package com.mixelte.melodorium.player

import androidx.media3.common.MediaItem
import com.mixelte.melodorium.domain.models.MusicFile


class PlayerItem(val file: MusicFile) {
    val mediaId = (lastMediaId++).toString()
    val mediaItem = MediaItem.Builder().setUri(file.uri).setMediaId(this.mediaId).build()

    companion object {
        var lastMediaId = 1
    }
}
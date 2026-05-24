package com.mixelte.melodorium.ui.theme

import androidx.compose.ui.graphics.Color
import com.mixelte.melodorium.domain.models.MusicMood

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Color.muted get() = this.copy(alpha = 0.75f)
val Color.Companion.Tomato get() = Color.hsl(9f, 1f, 0.64f)

fun MusicMood.getBackgroundColor(): Color = when (this) {
    MusicMood.Rock -> Color(0xFFD6583B)
    MusicMood.Energistic -> Color(0xFFE3DC44)
    MusicMood.Cheerful -> Color(0xFF3ED47B)
    MusicMood.Calm -> Color(0xFF4DBDE6)
    MusicMood.Sleep -> Color(0xFF3F36C9)
}

fun MusicMood.getStarColor(): Color = when (this) {
    MusicMood.Rock -> Color(0xFF2DE392)
    MusicMood.Energistic -> Color(0xFF1B3DDE)
    MusicMood.Cheerful -> Color(0xFFE02B33)
    MusicMood.Calm -> Color(0xFFE4F046)
    MusicMood.Sleep -> Color(0xFFE6812B)
}

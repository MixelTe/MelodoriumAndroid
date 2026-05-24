package com.mixelte.melodorium.domain.models

enum class MusicMood {
    Rock,
    Energistic,
    Cheerful,
    Calm,
    Sleep,
}

enum class MusicLike {
    Best,
    Like,
    Good,
    Normal,
}

enum class MusicLang {
    No,
    Ru,
    An,
    En,
    Fr,
    Ge,
    It,
    As,
}

enum class MusicEmo {
    Happy,
    Neutral,
    Sad,
}

enum class MusicPublic {
    Private,
    Public;

    companion object {
        fun fromBool(bool: Boolean) = when (bool) {
            true -> Public
            false -> Private
        }
    }
}
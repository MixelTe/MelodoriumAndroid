package com.mixelte.melodorium.domain.models

enum class MusicMood {
    Rock,
    Energistic,
    Cheerful,
    Calm,
    Sleep;

    fun toName(): String = when (this) {
        Rock -> "Рок"
        Energistic -> "Энергичное"
        Cheerful -> "Бодрое"
        Calm -> "Спокойное"
        Sleep -> "Сон"
    }
}

enum class MusicLike {
    Best,
    Like,
    Good,
    Normal;

    fun toName(): String = when (this) {
        Best -> "Избранное"
        Like -> "Любимое"
        Good -> "Приятное"
        Normal -> "Среднее"
    }
}

enum class MusicLang {
    No,
    Ru,
    An,
    En,
    Fr,
    Ge,
    It,
    As;

    fun toName(): String = when (this) {
        No -> "\uD83C\uDFB5"
        Ru -> "\uD83C\uDDF7\uD83C\uDDFA"
        An -> "\uD83C\uDF10"
        En -> "\uD83C\uDDEC\uD83C\uDDE7"
        Fr -> "\uD83C\uDDEB\uD83C\uDDF7"
        Ge -> "\uD83C\uDDE9\uD83C\uDDEA"
        It -> "\uD83C\uDDEE\uD83C\uDDF9"
        As -> "⛩\uFE0F"
    }
}

enum class MusicEmo {
    Happy,
    Neutral,
    Sad;

    fun toName(): String = when (this) {
        Happy -> "Веселое"
        Neutral -> "Нейтральное"
        Sad -> "Грустное"
    }
}

enum class MusicPublic {
    Private,
    Public;

    fun toName(): String = when (this) {
        Private -> "Приватный"
        Public -> "Публичный"
    }

    companion object {
        fun fromBool(bool: Boolean) = when (bool) {
            true -> Public
            false -> Private
        }
    }
}
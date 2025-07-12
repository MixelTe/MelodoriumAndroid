package com.mixelte.melodorium

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

fun Modifier.trueScale(scale: Float): Modifier {
    return this
        .scale(scale)
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val width = (placeable.width * scale).roundToInt()
            val height = (placeable.height * scale).roundToInt()
            layout(width, height) {
                placeable.place((width - placeable.width) / 2, (height - placeable.height) / 2)
            }
        }
}

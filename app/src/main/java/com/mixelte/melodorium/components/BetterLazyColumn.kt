package com.mixelte.melodorium.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class BetterLazyColumn<T> {
    var selectedItems by mutableStateOf(listOf<T>())

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
    @Composable
    fun LazyColumn(
        items: List<T>,
        itemKey: (T) -> Any,
        dropdownItems: @Composable ((T, () -> Unit) -> Unit),
        colors: @Composable ((T) -> CardColors?)?,
        onClick: (T) -> Any,
        item: @Composable ((T) -> Unit),
    ) {
        val haptics = LocalHapticFeedback.current
        val density = LocalDensity.current
        var lastCheckTime by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
        var lastCheckItem by remember { mutableStateOf(Pair<T?, T?>(null, null)) }

        LazyColumn {
            items(items, itemKey) { listItem ->
                var offset by remember { mutableStateOf(Offset.Zero) }
                var dropDownExpanded by remember { mutableStateOf(false) }
                val (xDp, yDp) = with(density) {
                    (offset.x.toDp()) to (offset.y.toDp())
                }
                val selected = listItem in selectedItems
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = colors?.let { it(listItem) } ?: CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier
                        .pointerInteropFilter {
                            offset = Offset(it.x, it.y)
                            false
                        }
                        .combinedClickable(
                            onClick = { onClick(listItem) },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                dropDownExpanded = true
                            })
                ) {
                    Row {
                        Checkbox(
                            selected,
                            {
                                val mark = TimeSource.Monotonic.markNow()
                                val prevMark = lastCheckTime
                                val (prevItem2, prevItem1) = lastCheckItem
                                lastCheckTime = mark
                                lastCheckItem = prevItem1 to listItem
                                if (prevItem2 != null && prevItem2 != listItem && prevItem1 == listItem
                                    && prevMark?.let { mark - it < 300.milliseconds } == true
                                ) {
                                    val anotherI = items.indexOf(prevItem2)
                                    val thisI = items.indexOf(listItem)
                                    val list = selectedItems.toMutableList()
                                    val select = prevItem2 in selectedItems
                                    for (i in min(thisI, anotherI)..max(thisI, anotherI)) {
                                        val item = items[i]
                                        if (select) {
                                            if (item !in list)
                                                list.add(item)
                                        } else {
                                            if (item in list)
                                                list.remove(item)
                                        }
                                    }
                                    selectedItems = list
                                } else {
                                    selectedItems =
                                        if (selected) selectedItems.toMutableList()
                                            .also { it.remove(listItem) }
                                        else selectedItems.toMutableList().also { it.add(listItem) }
                                }
                            },
                        )
                        item(listItem)
                        DropdownMenu(
                            expanded = dropDownExpanded,
                            offset = DpOffset(xDp, yDp),
                            onDismissRequest = { dropDownExpanded = false }) {

                            dropdownItems(listItem, { dropDownExpanded = false })
                        }
                    }
                }
            }
        }
    }
}
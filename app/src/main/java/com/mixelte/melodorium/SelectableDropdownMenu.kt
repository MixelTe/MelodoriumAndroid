package com.mixelte.melodorium

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectableDropdownMenu(
    items: List<T>,
    isSelected: (T) -> Boolean,
    setSelected: (T, Boolean) -> Unit,
    itemName: (T) -> String,
    content: @Composable () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        Box(modifier = Modifier.clickable { }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                content()
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            items.forEach { item ->
                AnimatedContent(isSelected(item)) { isSelected ->
                    if (isSelected) {
                        DropdownMenuItem(
                            text = {
                                Text(text = itemName(item))
                            },
                            onClick = {
                                setSelected(item, false)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null
                                )
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = {
                                Text(text = itemName(item))
                            },
                            onClick = {
                                setSelected(item, true)
                            },
                        )
                    }
                }
            }
        }
    }
}
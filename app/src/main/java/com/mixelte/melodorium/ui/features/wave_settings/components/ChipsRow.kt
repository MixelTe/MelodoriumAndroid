package com.mixelte.melodorium.ui.features.wave_settings.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.ui.features.wave_settings.WaveSettingsUiChip

@Composable
fun ChipsRow(
    items: List<WaveSettingsUiChip>,
    wrap: Boolean = false,
    toggle: (item: String) -> Unit
) {
    @Composable
    fun inner() {
        items.forEach { item ->
            FilterChip(
                selected = item.isSelected,
                onClick = {
                    toggle(item.id)
                },
                label = { Text(item.text) },
                leadingIcon = if (item.isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }

    if (wrap) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            inner()
        }
    } else {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            inner()
        }
    }
}
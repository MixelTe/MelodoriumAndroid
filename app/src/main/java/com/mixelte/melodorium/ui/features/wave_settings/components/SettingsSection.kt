package com.mixelte.melodorium.ui.features.wave_settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mixelte.melodorium.R

@Composable
fun SettingsSection(
    title: String,
    onClearClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onSelectAllClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
//                    painter = painterResource(id = R.drawable.ic_remove),
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Выбрать всё",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(
                    onClick = onClearClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_remove),
                        contentDescription = "Очистить секцию",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        content()
    }
}
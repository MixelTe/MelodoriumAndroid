package com.mixelte.melodorium.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.mixelte.melodorium.player.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextButtonSmall(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        TextButton(
            onClick,
            enabled = enabled,
            modifier = modifier.heightIn(max = with(LocalDensity.current) {
                MaterialTheme.typography.labelLarge.lineHeight.toDp() + (2 * 2.dp)
            }),
            contentPadding = PaddingValues(6.dp, 2.dp)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }

}
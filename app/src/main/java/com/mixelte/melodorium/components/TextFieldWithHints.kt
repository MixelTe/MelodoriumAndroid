package com.mixelte.melodorium.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithHints(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = false }) {
        OutlinedTextField(
            modifier = modifier.menuAnchor(),
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            label = label,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )

        val filterOpts = options.filter { it.contains(value, ignoreCase = true) }
        if (filterOpts.isNotEmpty()) {
            DropdownMenu(
                modifier = Modifier.exposedDropdownSize(),
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = false)
            ) {
                filterOpts.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
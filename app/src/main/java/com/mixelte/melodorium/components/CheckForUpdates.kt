package com.mixelte.melodorium.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun CheckForUpdates() {
    val coroutineScope = rememberCoroutineScope()
    var newVersion by remember { mutableStateOf<String?>(null) }
    var fetching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    if (fetching)
        return FilledTonalButton(onClick = {}) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Checking for updates")
                CircularProgressIndicator(
                    modifier = Modifier.height(16.dp).width(16.dp),
                )
            }
        }

    FilledTonalButton(onClick = {
        fetching = true
        error = false
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val url =
                    URL("https://raw.githubusercontent.com/MixelTe/MelodoriumAndroid/refs/heads/master/v.txt")
                val urlConnection = url.openConnection() as HttpURLConnection
                try {
                    val data = urlConnection.inputStream.bufferedReader().readLine()
                    newVersion = data
                } catch (x: Exception) {
                    error = true
                } finally {
                    urlConnection.disconnect()
                    fetching = false
                }
            }
        }
    }) {
        if (error)
            Text("Error occurred")
        else if (newVersion == null)
            Text("Check for updates")
        else if (newVersion == "1.0")
            Text("Is up to date")
        else
            Text("New version available: 1.0 -> $newVersion")
    }
}
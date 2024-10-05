package com.chaaba.composedownloadmanager.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DownloadScreen(
    viewModel: DownloadViewModel,
    modifier: Modifier = Modifier,
    onDownloadComplete: (Long) -> Unit
) {
    val progress by viewModel.downloadProgress.observeAsState(initial = 0)
    val downloadID by viewModel.downloadId.observeAsState(initial = 0)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { viewModel.startDownload() }) {
            Text(text = "Start Download")
        }

        when (progress) {
            in 1..99 -> {
                // Show a circular progress bar with current progress
                LoadingProgress(progress = progress, modifier = Modifier.size(200.dp))
            }

            100 -> {
                // When download completes
                Text(text = "Download Complete!")
                onDownloadComplete(downloadID)
            }

            -1 -> {
                // If download fails
                Text(text = "Download Failed")
            }
        }
    }
}

@Composable
fun LoadingProgress(modifier: Modifier = Modifier, progress: Int) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp) // Adjust the size of the progress bar
        )
        Text(text = "Progress: $progress %")
    }
}

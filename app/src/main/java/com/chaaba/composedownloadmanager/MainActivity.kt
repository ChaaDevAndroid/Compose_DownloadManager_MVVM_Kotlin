package com.chaaba.composedownloadmanager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.chaaba.composedownloadmanager.ui.DownloadScreen
import com.chaaba.composedownloadmanager.ui.DownloadViewModel
import com.chaaba.composedownloadmanager.ui.theme.ComposeDownloadManagerTheme
import kotlin.properties.Delegates

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: DownloadViewModel
    private var downloadID by Delegates.notNull<Long>()

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Fetch the download ID received in the broadcast
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1

            // Check if this broadcast is for our specific download
            if (downloadID == id) {
                // Show a toast message when the download completes
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        setContent {
            ComposeDownloadManagerTheme {
                val context = LocalContext.current
                viewModel = DownloadViewModel(context)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DownloadScreen(
                        modifier = Modifier.padding(innerPadding), viewModel = viewModel
                    ) {
                        downloadID = it
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}


package com.chaaba.composedownloadmanager.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class DownloadViewModel(private val context: Context) : ViewModel() {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val _downloadId = MutableLiveData<Long>()
    val downloadId = _downloadId

    private val _downloadProgress = MutableLiveData<Int>()
    val downloadProgress: LiveData<Int> get() = _downloadProgress

    private val url =
        "https://freetestdata.com/wp-content/uploads/2022/11/Free_Test_Data_10.5MB_PDF.pdf"

    // Extract the file name from the URL and capitalize the first letter
    private val fileName = url.substringAfterLast('/').replaceFirstChar { it.uppercase() }

    // Create the document file in the desired location
    private val file = createDocumentFile(fileName)

    fun startDownload() {
        val request = provideRequest()
        val downloadID = downloadManager.enqueue(request)
        _downloadId.postValue(downloadID)
        // Launch coroutine to monitor download progress
        viewModelScope.launch(Dispatchers.IO) {
            trackDownloadProgress(downloadID)
        }
    }

    private fun createDocumentFile(fileName: String): File {
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDirectory, fileName)
    }

    private fun provideRequest(): DownloadManager.Request =
        DownloadManager.Request(Uri.parse(url)).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(Uri.fromFile(file)) // Specify the destination URI
            setTitle(fileName) // Set the title for the download notification
            setDescription("Downloading") // Set the description for the download notification
            setRequiresCharging(false) // Set if charging is required to begin the download
            setAllowedOverMetered(true) // Allow downloads over mobile networks
            setAllowedOverRoaming(true) // Allow downloads over roaming networks
        }

    private fun trackDownloadProgress(downloadID: Long) {
        var finishDownload = false

        while (!finishDownload) {
            val cursor: Cursor =
                downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                        _downloadProgress.postValue(-1) // Failed status
                    }

                    DownloadManager.STATUS_PAUSED,
                    DownloadManager.STATUS_PENDING -> {
                        // You could add specific handling for paused/pending if needed
                    }

                    DownloadManager.STATUS_RUNNING -> {
                        val total =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val progress = ((downloaded * 100L) / total).toInt()
                            _downloadProgress.postValue(progress)
                        }
                    }

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        finishDownload = true
                        _downloadProgress.postValue(100) // Success
                    }
                }
            }
            cursor.close()
        }
    }


}

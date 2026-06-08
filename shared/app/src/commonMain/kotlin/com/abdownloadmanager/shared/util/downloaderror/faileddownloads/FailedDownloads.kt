package com.abdownloadmanager.shared.util.downloaderror.faileddownloads

import com.abdownloadmanager.shared.util.downloaderror.IDownloadErrorMapperRegistry
import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.util.guardedEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FailedDownloads(
    private val downloadManager: DownloadManagerMinimalControl,
    private val failedDownloadsStorage: IFailedDownloadErrorStorage,
    private val errorMapperRegistry: IDownloadErrorMapperRegistry,
    private val scope: CoroutineScope,
) {
    private val booted = guardedEntry()
    fun boot() {
        booted.action {
            scope.launch {
                downloadManager.listOfJobsEvents.collect {
                    onEvent(it)
                }
            }
        }
    }


    private fun onEvent(event: DownloadManagerEvents) {
        when (event) {
            is DownloadManagerEvents.OnJobStarted,
            is DownloadManagerEvents.OnJobAdded -> {
            }

            is DownloadManagerEvents.OnJobCanceled -> {
                errorMapperRegistry.getReason(event.e)?.let {
                    failedDownloadsStorage.setReason(event.downloadItem.id, it)
                }
            }

            is DownloadManagerEvents.OnJobChanged,
            is DownloadManagerEvents.OnJobCompleted,
            is DownloadManagerEvents.OnJobRemoved,
            is DownloadManagerEvents.OnJobStarting -> {
                failedDownloadsStorage.removeReason(event.downloadItem.id)
            }
        }
    }
}

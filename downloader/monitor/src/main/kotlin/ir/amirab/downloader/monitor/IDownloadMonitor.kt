package ir.amirab.downloader.monitor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IDownloadMonitor {
    var useAverageSpeed: Boolean
    val activeDownloadListFlow: MutableStateFlow<List<ProcessingDownloadItemState>>
    val completedDownloadListFlow: MutableStateFlow<List<CompletedDownloadItemState>>
    val downloadListFlow: Flow<List<IDownloadItemState>>
    val activeDownloadCount: StateFlow<Int>

    suspend fun waitForDownloadToFinishOrCancel(
        id: Long
    ): Boolean
}
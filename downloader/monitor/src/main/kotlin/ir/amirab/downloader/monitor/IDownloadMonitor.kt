package ir.amirab.downloader.monitor

import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.util.flow.mapStateFlow
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

fun IDownloadMonitor.isDownloadActiveFlow(
    downloadId: Long,
): StateFlow<Boolean> {
    return activeDownloadListFlow.mapStateFlow { activeDownloadList ->
        activeDownloadList.find {
            downloadId == it.id
        }?.statusOrFinished()?.let {
            it is DownloadJobStatus.IsActive
        } ?: false
    }
}
package ir.amirab.downloader

import ir.amirab.downloader.downloaditem.DownloadItemContext
import ir.amirab.downloader.downloaditem.EmptyContext
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.flow.SharedFlow

sealed interface DownloadManagerEvents {
    val downloadItem: IDownloadItem
    val context: DownloadItemContext

    data class OnJobAdded(
        override val downloadItem: IDownloadItem,
        override val context: DownloadItemContext
    ) : DownloadManagerEvents

    data class OnJobChanged(
        override val downloadItem: IDownloadItem,
        override val context: DownloadItemContext
    ) : DownloadManagerEvents

    data class OnJobStarting(
        override val downloadItem: IDownloadItem,
        override val context: DownloadItemContext
    ) : DownloadManagerEvents

    data class OnJobStarted(
        override val downloadItem: IDownloadItem,
        override val context: DownloadItemContext
    ) : DownloadManagerEvents

    data class OnJobCompleted(
        override val downloadItem: IDownloadItem,
        override val context: DownloadItemContext
    ) : DownloadManagerEvents

    data class OnJobCanceled(
        override val downloadItem: IDownloadItem,
        override val context: DownloadItemContext,
        val e: Throwable
    ) : DownloadManagerEvents

    data class OnJobRemoved(
        override val downloadItem: IDownloadItem,
        override val context: DownloadItemContext
    ) : DownloadManagerEvents
}

interface DownloadManagerMinimalControl {
    suspend fun startJob(id: Long, context: DownloadItemContext = EmptyContext)
    suspend fun stopJob(id: Long, context: DownloadItemContext = EmptyContext)
    fun canActivateJob(id: Long): Boolean
    val listOfJobsEvents: SharedFlow<DownloadManagerEvents>
}

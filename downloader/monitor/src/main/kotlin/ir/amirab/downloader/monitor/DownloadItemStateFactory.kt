package ir.amirab.downloader.monitor

import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadItem

interface DownloadItemStateFactory<
        in TDownloadItem : IDownloadItem,
        in TDownloadJob : DownloadJob
        > {
    fun createProcessingDownloadItemStateFromDownloadJob(
        downloadJob: TDownloadJob,
        speed: Long,
    ): ProcessingDownloadItemState

    fun createCompletedDownloadItemStateFromDownloadItem(
        downloadItem: TDownloadItem,
    ): CompletedDownloadItemState

}

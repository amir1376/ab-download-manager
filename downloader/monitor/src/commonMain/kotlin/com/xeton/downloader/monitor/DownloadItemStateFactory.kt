package com.xeton.downloader.monitor

import androidx.compose.runtime.Immutable
import com.xeton.downloader.downloaditem.DownloadJob
import com.xeton.downloader.downloaditem.IDownloadItem

@Immutable
data class ProcessingDownloadItemFactoryInputs<
        out TDownloadJob : DownloadJob
        >(
    val downloadJob: TDownloadJob,
    val speed: Long,
    val isWaiting: Boolean,
)

interface DownloadItemStateFactory<
        in TDownloadItem : IDownloadItem,
        in TDownloadJob : DownloadJob
        > {
    fun createProcessingDownloadItemState(
        props: ProcessingDownloadItemFactoryInputs<TDownloadJob>
    ): ProcessingDownloadItemState

    fun createCompletedDownloadItemState(
        downloadItem: TDownloadItem,
    ): CompletedDownloadItemState

}

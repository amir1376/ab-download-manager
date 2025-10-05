package ir.amirab.downloader.downloaditem

import ir.amirab.downloader.utils.ExceptionUtils

sealed class DownloadJobStatus(
    val order: Int,
    private val downloadStatus: DownloadStatus
) {
    fun asDownloadStatus() = downloadStatus

    data object Downloading : DownloadJobStatus(0, DownloadStatus.Downloading),
        IsActive

    data class Retrying(val timeUntilRetry: Long) : DownloadJobStatus(0, DownloadStatus.Paused),
        IsActive

    data object Resuming : DownloadJobStatus(0, DownloadStatus.Downloading),
        IsActive

    data class PreparingFile(val percent: Int?) : DownloadJobStatus(1, DownloadStatus.Downloading),
        IsActive

    data class Canceled(val e: Throwable) : DownloadJobStatus(
        2,
        if (ExceptionUtils.isNormalCancellation(e)) DownloadStatus.Paused else DownloadStatus.Error
    ),
        CanBeResumed

    data object IDLE : DownloadJobStatus(2, DownloadStatus.Added),
        CanBeResumed

    data object Finished : DownloadJobStatus(3, DownloadStatus.Completed)

    interface IsActive
    interface CanBeResumed
}

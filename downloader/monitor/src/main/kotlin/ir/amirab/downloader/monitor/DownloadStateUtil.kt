package ir.amirab.downloader.monitor

import ir.amirab.downloader.downloaditem.DownloadJobStatus

fun IDownloadItemState.statusOrFinished(): DownloadJobStatus {
    return (this as? ProcessingDownloadItemState)?.status ?: DownloadJobStatus.Finished
}

fun IDownloadItemState.isFinished(): Boolean {
    return this is CompletedDownloadItemState
}

fun IDownloadItemState.isNotFinished(): Boolean {
    return this is ProcessingDownloadItemState
}

fun IDownloadItemState.speedOrNull(): Long? {
    return (this as? ProcessingDownloadItemState)?.speed
}

fun IDownloadItemState.remainingOrNull(): Long? {
    return (this as? ProcessingDownloadItemState)?.remainingTime
}

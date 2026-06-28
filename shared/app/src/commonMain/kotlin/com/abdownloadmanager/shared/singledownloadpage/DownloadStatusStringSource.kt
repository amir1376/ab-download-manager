package com.abdownloadmanager.shared.singledownloadpage

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import com.xeton.downloader.downloaditem.DownloadJobStatus
import com.xeton.downloader.monitor.IDownloadItemState
import com.xeton.downloader.monitor.ProcessingDownloadItemState
import com.xeton.downloader.monitor.statusOrFinished
import com.xeton.downloader.utils.ExceptionUtils
import com.xeton.util.compose.StringSource
import com.xeton.util.compose.asStringSource

fun createStatusString(
    it: IDownloadItemState,
): StringSource {
    if (it is ProcessingDownloadItemState && it.isWaiting) {
        return Res.string.waiting.asStringSource()
    }
    return when (val status = it.statusOrFinished()) {
        is DownloadJobStatus.Canceled -> {
            if (ExceptionUtils.isNormalCancellation(status.e)) {
                Res.string.paused
            } else {
                Res.string.error
            }
        }

        DownloadJobStatus.Downloading -> Res.string.downloading
        DownloadJobStatus.Finished -> Res.string.finished
        DownloadJobStatus.IDLE -> Res.string.idle
        is DownloadJobStatus.PreparingFile -> Res.string.preparing_file
        DownloadJobStatus.Resuming -> Res.string.resuming
        is DownloadJobStatus.Retrying -> Res.string.retrying
    }.asStringSource()
}

fun createStatusStringWithReason(
    it: IDownloadItemState,
    errorReason: DownloadErrorReason?,
): StringSource {
    if (it is ProcessingDownloadItemState && it.isWaiting) {
        return Res.string.waiting.asStringSource()
    }
    return when (val status = it.statusOrFinished()) {
        is DownloadJobStatus.Canceled -> {
            if (ExceptionUtils.isNormalCancellation(status.e)) {
                Res.string.paused.asStringSource()
            } else {
                errorReason?.title?.asStringSource()
                    ?: Res.string.error.asStringSource()
            }
        }

        DownloadJobStatus.Downloading -> Res.string.downloading.asStringSource()
        DownloadJobStatus.Finished -> Res.string.finished.asStringSource()
        DownloadJobStatus.IDLE -> Res.string.idle.asStringSource()
        is DownloadJobStatus.PreparingFile -> Res.string.preparing_file.asStringSource()
        DownloadJobStatus.Resuming -> Res.string.resuming.asStringSource()
        is DownloadJobStatus.Retrying -> Res.string.retrying.asStringSource()
    }
}

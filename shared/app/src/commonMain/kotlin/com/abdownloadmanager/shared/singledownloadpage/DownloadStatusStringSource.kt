package com.abdownloadmanager.shared.singledownloadpage

import com.abdownloadmanager.resources.Res
import ir.amirab.downloader.downloaditem.DownloadJobStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.downloader.utils.ExceptionUtils
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.asStringSource

fun createStatusString(it: IDownloadItemState): StringSource {
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

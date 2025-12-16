package com.abdownloadmanager.shared.pages.home.category

import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.statusOrFinished
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource

class DownloadStatusCategoryFilterByList(
    name: StringSource,
    icon: IconSource,
    val acceptedStatus: List<DownloadStatus>,
) : DownloadStatusCategoryFilter(name, icon) {
    override fun accept(iDownloadStatus: IDownloadItemState): Boolean {
        return iDownloadStatus
            .statusOrFinished()
            .asDownloadStatus() in acceptedStatus
    }
}

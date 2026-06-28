package com.abdownloadmanager.shared.pages.home.category

import com.xeton.downloader.downloaditem.DownloadStatus
import com.xeton.downloader.monitor.IDownloadItemState
import com.xeton.downloader.monitor.statusOrFinished
import com.xeton.util.compose.IconSource
import com.xeton.util.compose.StringSource

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

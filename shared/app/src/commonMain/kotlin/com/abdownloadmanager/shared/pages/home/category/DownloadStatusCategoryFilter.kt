package com.abdownloadmanager.shared.pages.home.category

import com.xeton.downloader.monitor.IDownloadItemState
import com.xeton.util.compose.IconSource
import com.xeton.util.compose.StringSource

abstract class DownloadStatusCategoryFilter(
    val name: StringSource,
    val icon: IconSource,
) {
    abstract fun accept(iDownloadStatus: IDownloadItemState): Boolean
}

package com.abdownloadmanager.shared.pages.home.category

import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.util.compose.IconSource
import ir.amirab.util.compose.StringSource

abstract class DownloadStatusCategoryFilter(
    val name: StringSource,
    val icon: IconSource,
) {
    abstract fun accept(iDownloadStatus: IDownloadItemState): Boolean
}

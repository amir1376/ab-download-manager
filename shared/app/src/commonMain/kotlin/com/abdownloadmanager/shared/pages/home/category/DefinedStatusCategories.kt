package com.abdownloadmanager.shared.pages.home.category

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.util.compose.asStringSource

object DefinedStatusCategories {
    fun values() = listOf(All, Finished, Unfinished)


    val All = object : DownloadStatusCategoryFilter(
        Res.string.all.asStringSource(),
        MyIcons.folder,
    ) {
        override fun accept(iDownloadStatus: IDownloadItemState): Boolean = true
    }
    val Finished = DownloadStatusCategoryFilterByList(
        Res.string.finished.asStringSource(),
        MyIcons.folder,
        listOf(DownloadStatus.Completed)
    )
    val Unfinished = DownloadStatusCategoryFilterByList(
        Res.string.Unfinished.asStringSource(),
        MyIcons.folder,
        listOf(
            DownloadStatus.Error,
            DownloadStatus.Added,
            DownloadStatus.Paused,
            DownloadStatus.Downloading,
        )
    )
}

package com.abdownloadmanager.shared.pages.home.category

import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.xeton.downloader.downloaditem.DownloadStatus
import com.xeton.downloader.monitor.IDownloadItemState
import com.xeton.util.compose.asStringSource

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
        MyIcons.folderFinished,
        listOf(DownloadStatus.Completed)
    )
    val Unfinished = DownloadStatusCategoryFilterByList(
        Res.string.Unfinished.asStringSource(),
        MyIcons.folderUnfinished,
        listOf(
            DownloadStatus.Error,
            DownloadStatus.Added,
            DownloadStatus.Paused,
            DownloadStatus.Downloading,
        )
    )
}

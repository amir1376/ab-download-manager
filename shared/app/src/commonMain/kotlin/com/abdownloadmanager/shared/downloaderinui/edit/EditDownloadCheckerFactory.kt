package com.abdownloadmanager.shared.downloaderinui.edit

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.xeton.downloader.connection.IResponseInfo
import com.xeton.downloader.downloaditem.IDownloadCredentials
import com.xeton.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

interface EditDownloadCheckerFactory<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TDownloadSize : DownloadSize,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo, TDownloadSize>> {
    fun createEditDownloadChecker(
        currentDownloadItem: MutableStateFlow<TDownloadItem>,
        editedDownloadItem: MutableStateFlow<TDownloadItem>,
        linkChecker: TLinkChecker,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope,
    ): EditDownloadChecker<TDownloadItem, TCredentials, TResponseInfo, TDownloadSize, TLinkChecker>
}

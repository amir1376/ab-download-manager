package com.abdownloadmanager.shared.downloaderinui.edit

import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.downloaderinui.TADownloaderInUI
import com.abdownloadmanager.shared.downloaderinui.http.edit.EditDownloadChecker
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

interface EditDownloadCheckerFactory<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo>> {
    fun createEditDownloadChecker(
        currentDownloadItem: MutableStateFlow<TDownloadItem>,
        editedDownloadItem: MutableStateFlow<TDownloadItem>,
        linkChecker: TLinkChecker,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope,
    ): EditDownloadChecker<TDownloadItem, TCredentials, TResponseInfo, TLinkChecker>
}

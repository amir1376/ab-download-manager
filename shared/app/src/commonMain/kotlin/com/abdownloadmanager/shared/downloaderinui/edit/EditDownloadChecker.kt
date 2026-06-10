package com.abdownloadmanager.shared.downloaderinui.edit

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class EditDownloadChecker<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TDownloadSize : DownloadSize,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo, TDownloadSize>
        >(
    val currentDownloadItem: MutableStateFlow<TDownloadItem>,
    val editedDownloadItem: MutableStateFlow<TDownloadItem>,
    val linkChecker: TLinkChecker,
    val conflictDetector: DownloadConflictDetector,
    val scope: CoroutineScope,
) {
    abstract fun check()

    protected val _canEditResult = MutableStateFlow<CanEditDownloadResult>(CanEditDownloadResult.NothingChanged)
    val canEditResult = _canEditResult.asStateFlow()
    val canEdit = canEditResult.mapStateFlow {
        it is CanEditDownloadResult.CanEdit
    }
}

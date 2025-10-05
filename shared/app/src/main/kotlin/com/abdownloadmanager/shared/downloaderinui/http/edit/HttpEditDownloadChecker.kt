package com.abdownloadmanager.shared.downloaderinui.http.edit

import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditDownloadResult
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditWarnings
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpLinkChecker
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.util.FileNameValidator
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class EditDownloadChecker<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo>
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

class HttpEditDownloadChecker(
    currentDownloadItem: MutableStateFlow<HttpDownloadItem>,
    editedDownloadItem: MutableStateFlow<HttpDownloadItem>,
    conflictDetector: DownloadConflictDetector,
    scope: CoroutineScope,
    linkChecker: HttpLinkChecker,
) : EditDownloadChecker<HttpDownloadItem, HttpDownloadCredentials, HttpResponseInfo, HttpLinkChecker>(
    currentDownloadItem = currentDownloadItem,
    editedDownloadItem = editedDownloadItem,
    conflictDetector = conflictDetector,
    scope = scope,
    linkChecker = linkChecker
) {
    init {
        editedDownloadItem
            .onEach {
                _canEditResult.value = CanEditDownloadResult.Waiting
            }.launchIn(scope)
    }

    override fun check() {
        _canEditResult.value = CanEditDownloadResult.Waiting
        _canEditResult.value = check(
            current = currentDownloadItem.value,
            edited = editedDownloadItem.value,
            newLength = linkChecker.length.value,
        )
    }

    private fun check(
        current: HttpDownloadItem,
        edited: HttpDownloadItem,
        newLength: Long?,
    ): CanEditDownloadResult {
        if (current == edited) {
            return CanEditDownloadResult.NothingChanged
        }
        if (!HttpUrlUtils.isValidUrl(edited.link)) {
            return CanEditDownloadResult.InvalidURL
        }
        if (edited.name != current.name) {
            if (!FileNameValidator.isValidFileName(edited.name)) {
                return CanEditDownloadResult.InvalidFileName
            }
            if (conflictDetector.checkAlreadyExists(current, edited)) {
                return CanEditDownloadResult.FileNameAlreadyExists
            }
        }
        val warnings = mutableListOf<CanEditWarnings>()
        if (current.contentLength != newLength) {
            warnings.add(
                CanEditWarnings.FileSizeNotMatch(
                    currentSize = current.contentLength,
                    newSize = newLength ?: IDownloadItem.Companion.LENGTH_UNKNOWN,
                )
            )
        }
        return CanEditDownloadResult.CanEdit(warnings)
    }
}

package com.abdownloadmanager.shared.downloaderinui.http.edit

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditDownloadResult
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditWarnings
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadChecker
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpLinkChecker
import com.xeton.downloader.connection.response.HttpResponseInfo
import com.xeton.downloader.downloaditem.IDownloadItem
import com.xeton.downloader.downloaditem.http.HttpDownloadCredentials
import com.xeton.downloader.downloaditem.http.HttpDownloadItem
import com.xeton.util.FileNameValidator
import com.xeton.util.HttpUrlUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HttpEditDownloadChecker(
    currentDownloadItem: MutableStateFlow<HttpDownloadItem>,
    editedDownloadItem: MutableStateFlow<HttpDownloadItem>,
    conflictDetector: DownloadConflictDetector,
    scope: CoroutineScope,
    linkChecker: HttpLinkChecker,
) : EditDownloadChecker<HttpDownloadItem, HttpDownloadCredentials, HttpResponseInfo, DownloadSize.Bytes, HttpLinkChecker>(
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
            newLength = linkChecker.downloadSize.value?.bytes,
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

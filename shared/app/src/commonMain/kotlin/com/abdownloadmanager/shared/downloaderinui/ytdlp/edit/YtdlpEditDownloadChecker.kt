package com.abdownloadmanager.shared.downloaderinui.ytdlp.edit

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditDownloadResult
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import com.abdownloadmanager.shared.downloaderinui.edit.EditDownloadChecker
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditWarnings
import com.abdownloadmanager.shared.downloaderinui.ytdlp.YtdlpLinkChecker
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadCredentials
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadItem
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpResponseInfo
import ir.amirab.util.FileNameValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class YtdlpEditDownloadChecker(
    currentDownloadItem: MutableStateFlow<YtdlpDownloadItem>,
    editedDownloadItem: MutableStateFlow<YtdlpDownloadItem>,
    conflictDetector: DownloadConflictDetector,
    scope: CoroutineScope,
    linkChecker: YtdlpLinkChecker,
) : EditDownloadChecker<YtdlpDownloadItem, YtdlpDownloadCredentials, YtdlpResponseInfo, DownloadSize.Bytes, YtdlpLinkChecker>(
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
        )
    }

    private fun check(
        current: YtdlpDownloadItem,
        edited: YtdlpDownloadItem,
    ): CanEditDownloadResult {
        if (current == edited) {
            return CanEditDownloadResult.NothingChanged
        }
        if (edited.link.isBlank()) {
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
        return CanEditDownloadResult.CanEdit(warnings)
    }
}

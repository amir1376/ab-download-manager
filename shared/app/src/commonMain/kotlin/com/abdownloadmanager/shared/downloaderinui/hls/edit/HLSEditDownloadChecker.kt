package com.abdownloadmanager.shared.downloaderinui.hls.edit

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditDownloadResult
import com.abdownloadmanager.shared.downloaderinui.edit.CanEditWarnings
import com.abdownloadmanager.shared.downloaderinui.edit.DownloadConflictDetector
import ir.amirab.downloader.downloaditem.hls.HLSDownloadCredentials
import com.abdownloadmanager.shared.downloaderinui.hls.HLSLinkChecker
import ir.amirab.downloader.downloaditem.hls.HLSResponseInfo
import com.abdownloadmanager.shared.downloaderinui.http.edit.EditDownloadChecker
import ir.amirab.downloader.downloaditem.hls.HLSDownloadItem
import ir.amirab.util.FileNameValidator
import ir.amirab.util.HttpUrlUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HLSEditDownloadChecker(
    currentDownloadItem: MutableStateFlow<HLSDownloadItem>,
    editedDownloadItem: MutableStateFlow<HLSDownloadItem>,
    linkChecker: HLSLinkChecker,
    conflictDetector: DownloadConflictDetector,
    scope: CoroutineScope
) : EditDownloadChecker<HLSDownloadItem, HLSDownloadCredentials, HLSResponseInfo, DownloadSize.Duration, HLSLinkChecker>(
    currentDownloadItem = currentDownloadItem,
    editedDownloadItem = editedDownloadItem,
    linkChecker = linkChecker,
    conflictDetector = conflictDetector,
    scope = scope,
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
            newDuration = linkChecker.duration.value,
        )
    }

    private fun check(
        current: HLSDownloadItem,
        edited: HLSDownloadItem,
        newDuration: Double?,
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
        if (current.duration != newDuration) {
            warnings.add(
                CanEditWarnings.DurationNotMatch(
                    current.duration,
                    newDuration,
                )
            )
        }
        return CanEditDownloadResult.CanEdit(warnings)
    }
}

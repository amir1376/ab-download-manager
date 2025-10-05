package com.abdownloadmanager.shared.downloaderinui.edit

sealed interface CanEditDownloadResult {
    data object FileNameAlreadyExists : CanEditDownloadResult
    data object InvalidURL : CanEditDownloadResult
    data object InvalidFileName : CanEditDownloadResult
    data object NothingChanged : CanEditDownloadResult
    data object Waiting : CanEditDownloadResult
    data class CanEdit(
        val warnings: List<CanEditWarnings>,
    ) : CanEditDownloadResult
}

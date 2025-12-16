package com.abdownloadmanager.shared.downloaderinui.add

sealed interface CanAddResult {
    data class DownloadAlreadyExists(val itemId: Long) : CanAddResult
    data object InvalidFileName : CanAddResult
    data object CantWriteInThisFolder : CanAddResult

    data object InvalidUrl : CanAddResult
    data object CanAdd : CanAddResult
}

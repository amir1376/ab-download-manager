package com.abdownloadmanager.desktop.pages.singleDownloadPage

import arrow.optics.optics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

interface SingleDownloadPageStateStorage {
    val singleDownloadPageState: MutableStateFlow<SingleDownloadPageStateToPersist>
}
@optics
@Serializable
data class SingleDownloadPageStateToPersist(
    val showPartInfo: Boolean = false,
) {
    companion object
}

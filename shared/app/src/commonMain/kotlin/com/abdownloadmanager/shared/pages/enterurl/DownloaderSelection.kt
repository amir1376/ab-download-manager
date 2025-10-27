package com.abdownloadmanager.shared.pages.enterurl

import com.abdownloadmanager.shared.downloaderinui.TADownloaderInUI

sealed interface DownloaderSelection {
    data object Auto : DownloaderSelection
    data class Fixed(
        val downloaderInUi: TADownloaderInUI,
    ) : DownloaderSelection
}

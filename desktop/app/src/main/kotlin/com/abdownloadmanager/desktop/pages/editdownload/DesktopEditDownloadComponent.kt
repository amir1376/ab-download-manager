package com.abdownloadmanager.desktop.pages.editdownload

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pages.editdownload.BaseEditDownloadComponent
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent

sealed interface EditDownloadPageEffects {
    data object BringToFront : EditDownloadPageEffects
}

class DesktopEditDownloadComponent(
    ctx: ComponentContext,
    onRequestClose: () -> Unit,
    downloadId: Long,
    acceptEdit: StateFlow<Boolean>,
    onEdited: ((IDownloadItem) -> Unit, DownloadJobExtraConfig?) -> Unit,
    downloadSystem: DownloadSystem,
    downloaderInUiRegistry: DownloaderInUiRegistry,
    iconProvider: FileIconProvider,
) : BaseEditDownloadComponent(
    ctx = ctx,
    downloadSystem = downloadSystem,
    downloaderInUiRegistry = downloaderInUiRegistry,
    iconProvider = iconProvider,
    onEdited = onEdited,
    onRequestClose = onRequestClose,
    downloadId = downloadId,
    acceptEdit = acceptEdit,
),
    ContainsEffects<EditDownloadPageEffects> by supportEffects(),
    KoinComponent {
    fun bringToFront() {
        sendEffect(EditDownloadPageEffects.BringToFront)
    }
}

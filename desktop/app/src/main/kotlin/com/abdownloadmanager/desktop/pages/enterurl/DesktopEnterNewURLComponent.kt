package com.abdownloadmanager.desktop.pages.enterurl

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pages.enterurl.BaseEnterNewURLComponent
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials

class DesktopEnterNewURLComponent(
    ctx: ComponentContext,
    config: Config,
    downloaderInUiRegistry: DownloaderInUiRegistry,
    onCloseRequest: () -> Unit,
    onRequestFinished: (IDownloadCredentials) -> Unit,
) : BaseEnterNewURLComponent(
    ctx = ctx,
    config = config,
    downloaderInUiRegistry = downloaderInUiRegistry,
    onCloseRequest = onCloseRequest,
    onRequestFinished = onRequestFinished,
) {
    sealed interface Effects : BaseEnterNewURLComponent.Effects.PlatformEffects {
        data object BringToFront : Effects
    }

    data object Config : BaseEnterNewURLComponent.Config

    fun bringToFront() {
        sendEffect(Effects.BringToFront)
    }
}


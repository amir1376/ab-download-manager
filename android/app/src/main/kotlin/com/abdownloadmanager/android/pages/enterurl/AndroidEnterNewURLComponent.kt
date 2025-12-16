package com.abdownloadmanager.android.pages.enterurl

import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.pages.enterurl.BaseEnterNewURLComponent
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.downloaditem.IDownloadCredentials

class AndroidEnterNewURLComponent(
    ctx: ComponentContext,
    config: AndroidEnterNewURLComponent.Config,
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
    object Config : BaseEnterNewURLComponent.Config

    override val shouldFillWithClipboard: Boolean = false
}

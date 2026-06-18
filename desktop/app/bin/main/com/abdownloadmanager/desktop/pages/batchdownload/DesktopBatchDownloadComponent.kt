package com.abdownloadmanager.desktop.pages.batchdownload

import com.abdownloadmanager.shared.pages.batchdownload.BaseBatchDownloadComponent
import com.arkivanov.decompose.ComponentContext

class DesktopBatchDownloadComponent(
    ctx: ComponentContext,
    onClose: () -> Unit,
    importLinks: (List<String>) -> Unit,
) : BaseBatchDownloadComponent(
    ctx = ctx,
    onClose = onClose,
    importLinks = importLinks
) {
    fun bringToFront() {
        sendEffect(Effects.BringToFront)
    }

    sealed interface Effects : BaseBatchDownloadComponent.Effects.PlatformEffects {
        data object BringToFront : Effects
    }
}

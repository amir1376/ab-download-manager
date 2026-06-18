package com.abdownloadmanager.desktop.pages.checksum

import com.abdownloadmanager.shared.pages.checksum.BaseFileChecksumComponent
import com.abdownloadmanager.shared.util.DownloadSystem
import com.arkivanov.decompose.ComponentContext
import java.util.UUID

class DesktopFileChecksumComponent(
    ctx: ComponentContext,
    id: String,
    itemIds: List<Long>,
    closeComponent: () -> Unit,
    downloadSystem: DownloadSystem
) : BaseFileChecksumComponent(
    ctx = ctx,
    id = id,
    itemIds = itemIds,
    closeComponent = closeComponent,
    downloadSystem = downloadSystem,
) {
    fun bringToFront() {
        sendEffect(Effects.BringToFront)
    }

    data class Config(
        val id: String = UUID.randomUUID().toString(),
        override val itemIds: List<Long>,
    ) : BaseFileChecksumComponent.Config

    sealed interface Effects : BaseFileChecksumComponent.Effects.Platform {
        data object BringToFront : Effects
    }
}

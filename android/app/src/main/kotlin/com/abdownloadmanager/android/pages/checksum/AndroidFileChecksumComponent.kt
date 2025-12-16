package com.abdownloadmanager.android.pages.checksum

import com.abdownloadmanager.shared.pages.checksum.BaseFileChecksumComponent
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.util.FileIconProvider
import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import java.util.UUID

class AndroidFileChecksumComponent(
    ctx: ComponentContext,
    id: String,
    itemIds: List<Long>,
    closeComponent: () -> Unit,
    downloadSystem: DownloadSystem,
    val iconProvider: FileIconProvider,
) : BaseFileChecksumComponent(
    ctx = ctx,
    id = id,
    itemIds = itemIds,
    closeComponent = closeComponent,
    downloadSystem = downloadSystem,
) {
    @Serializable
    data class Config(
        val id: String = UUID.randomUUID().toString(),
        override val itemIds: List<Long>,
    ) : BaseFileChecksumComponent.Config

    sealed interface Effects : BaseFileChecksumComponent.Effects.Platform {
        data object BringToFront : Effects
    }
}

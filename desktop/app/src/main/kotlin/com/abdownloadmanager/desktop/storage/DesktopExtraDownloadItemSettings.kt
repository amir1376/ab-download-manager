package com.abdownloadmanager.desktop.storage

import com.abdownloadmanager.shared.storage.IExtraDownloadItemSettings
import ir.amirab.util.desktop.poweraction.ContainsPowerActionConfigOnFinish
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
data class DesktopExtraDownloadItemSettings(
    override val id: Long,
    override val finalDestinationFolder: String? = null,
    override val finalDestinationName: String? = null,
    val powerActionTypeOnFinish: PowerActionConfig.Type? = null,
    val powerActionUseForceOnFinish: Boolean = false,
) : IExtraDownloadItemSettings, ContainsPowerActionConfigOnFinish {

    override fun copyWithFinalDestination(
        folder: String?,
        name: String?
    ): IExtraDownloadItemSettings {
        return this.copy(finalDestinationFolder = folder, finalDestinationName = name)
    }

    override fun getPowerActionConfigOnFinish() = powerActionTypeOnFinish?.let {
        PowerActionConfig(
            powerActionTypeOnFinish,
            powerActionUseForceOnFinish,
        )
    }

    companion object : IExtraDownloadItemSettings.DataClassDefinitions<DesktopExtraDownloadItemSettings> {
        override fun createDefault(id: Long) = DesktopExtraDownloadItemSettings(id = id)
        override val serializer: KSerializer<DesktopExtraDownloadItemSettings> =
            DesktopExtraDownloadItemSettings.serializer()
    }
}

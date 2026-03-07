package com.abdownloadmanager.android.storage

import com.abdownloadmanager.shared.storage.IExtraDownloadItemSettings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
data class AndroidExtraDownloadItemSettings(
    override val id: Long,
    override val finalDestinationFolder: String? = null,
    override val finalDestinationName: String? = null,
    // turnOffWifi: Boolean
) : IExtraDownloadItemSettings {

    override fun copyWithFinalDestination(
        folder: String?,
        name: String?
    ): IExtraDownloadItemSettings {
        return this.copy(finalDestinationFolder = folder, finalDestinationName = name)
    }

    companion object : IExtraDownloadItemSettings.DataClassDefinitions<AndroidExtraDownloadItemSettings> {
        override fun createDefault(id: Long) = AndroidExtraDownloadItemSettings(id = id)
        override val serializer: KSerializer<AndroidExtraDownloadItemSettings> =
            AndroidExtraDownloadItemSettings.serializer()
    }
}

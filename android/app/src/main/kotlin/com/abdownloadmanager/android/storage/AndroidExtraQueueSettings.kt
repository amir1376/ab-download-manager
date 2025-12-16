package com.abdownloadmanager.android.storage

import com.abdownloadmanager.shared.storage.IExtraQueueSettings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
data class AndroidExtraQueueSettings(
    override val id: Long,
) : IExtraQueueSettings {

    companion object : IExtraQueueSettings.DataClassDefinitions<AndroidExtraQueueSettings> {
        override fun createDefault(id: Long) = AndroidExtraQueueSettings(id)
        override val serializer: KSerializer<AndroidExtraQueueSettings> = AndroidExtraQueueSettings.serializer()
    }
}

package com.abdownloadmanager.shared.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

interface IExtraQueueSettingsStorage<T : IExtraQueueSettings> {
    suspend fun deleteExtraQueueSettings(queueId: Long)
    suspend fun setExtraQueueSettings(extraQueueSettings: T)
    fun getExtraQueueSettings(queueId: Long): T
    fun getExternalQueueSettingsAsFlow(id: Long, initialEmit: Boolean = false): Flow<T>
}

interface IExtraQueueSettings {
    val id: Long

    interface DataClassDefinitions<T : IExtraQueueSettings> {
        fun createDefault(id: Long): T
        val serializer: KSerializer<T>
    }
}

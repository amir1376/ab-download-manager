package com.abdownloadmanager.shared.storage

import kotlinx.coroutines.flow.Flow

interface IExtraQueueSettingsStorage<T> {
    suspend fun deleteExtraQueueSettings(queueId: Long)
    suspend fun setExtraQueueSettings(extraQueueSettings: T)
    fun getExtraQueueSettings(queueId: Long): T
    fun getExternalQueueSettingsAsFlow(id: Long, initialEmit: Boolean = false): Flow<T>
}

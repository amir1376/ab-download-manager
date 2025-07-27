package com.abdownloadmanager.shared.storage

import kotlinx.coroutines.flow.Flow

interface IExtraDownloadSettingsStorage<T> {
    suspend fun deleteExtraDownloadItemSettings(downloadId: Long)
    suspend fun setExtraDownloadItemSettings(extraDownloadItemSettings: T)
    fun getExtraDownloadItemSettings(downloadId: Long): T
    fun getExternalDownloadItemSettingsAsFlow(id: Long, initialEmit: Boolean = false): Flow<T>
}

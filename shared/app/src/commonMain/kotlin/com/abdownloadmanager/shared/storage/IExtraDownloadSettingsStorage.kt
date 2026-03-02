package com.abdownloadmanager.shared.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

interface IExtraDownloadSettingsStorage<T : IExtraDownloadItemSettings> {
    suspend fun deleteExtraDownloadItemSettings(downloadId: Long)
    suspend fun setExtraDownloadItemSettings(extraDownloadItemSettings: T)
    fun getExtraDownloadItemSettings(downloadId: Long): T
    fun getExternalDownloadItemSettingsAsFlow(id: Long, initialEmit: Boolean = false): Flow<T>
}

interface IExtraDownloadItemSettings {
    val id: Long
    val finalDestinationFolder: String?
    val finalDestinationName: String?

    fun copyWithFinalDestination(folder: String?, name: String?): IExtraDownloadItemSettings


    interface DataClassDefinitions<T : IExtraDownloadItemSettings> {
        fun createDefault(id: Long): T
        val serializer: KSerializer<T>
    }
}

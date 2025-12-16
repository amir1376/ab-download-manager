package com.abdownloadmanager.shared.storage

import ir.amirab.downloader.db.TransactionalFileSaver
import ir.amirab.downloader.utils.SuspendLockList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class ExtraDownloadSettingsStorage<T : IExtraDownloadItemSettings>(
    private val folder: File,
    private val transactionalFileSaver: TransactionalFileSaver,
    private val dataClassDefinitions: IExtraDownloadItemSettings.DataClassDefinitions<T>
) : IExtraDownloadSettingsStorage<T> {
    private fun getFileOf(id: Long) = folder
        .resolve("${id}.json")

    private val updateLocks = SuspendLockList<Long>()
    private val lastEmits = MutableSharedFlow<T>(
        extraBufferCapacity = 64,// too big!
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun deleteExtraDownloadItemSettings(
        downloadId: Long,
    ) {
        lastEmits.tryEmit(
            dataClassDefinitions.createDefault(downloadId)
        )
        getFileOf(downloadId).delete()
    }

    override suspend fun setExtraDownloadItemSettings(
        extraDownloadItemSettings: T,
    ) {
        require(extraDownloadItemSettings.id >= 0) {
            "downloadId must be >= 0"
        }
        val file = getFileOf(extraDownloadItemSettings.id)
        lastEmits.tryEmit(extraDownloadItemSettings)
        return withContext(Dispatchers.IO) {
            updateLocks.withLock(extraDownloadItemSettings.id) {
                transactionalFileSaver.writeObject(
                    file,
                    extraDownloadItemSettings,
                    dataClassDefinitions.serializer
                )
            }
        }
    }

    override fun getExtraDownloadItemSettings(downloadId: Long): T {
        val file = getFileOf(downloadId)
        return transactionalFileSaver
            .readObject(file, dataClassDefinitions.serializer)
            ?: dataClassDefinitions.createDefault(downloadId)
    }

    override fun getExternalDownloadItemSettingsAsFlow(
        id: Long, initialEmit: Boolean,
    ): Flow<T> {
        return flow {
            if (initialEmit) {
                emit(getExtraDownloadItemSettings(id))
            }
            emitAll(lastEmits.filter { it.id == id })
        }
    }
}

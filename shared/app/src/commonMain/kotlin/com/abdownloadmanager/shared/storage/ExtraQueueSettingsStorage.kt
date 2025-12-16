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

class ExtraQueueSettingsStorage<T : IExtraQueueSettings>(
    private val folder: File,
    private val transactionalFileSaver: TransactionalFileSaver,
    private val dataClassDefinitions: IExtraQueueSettings.DataClassDefinitions<T>,
) : IExtraQueueSettingsStorage<T> {
    private fun getFileOf(id: Long) = folder
        .resolve("${id}.json")

    private val updateLocks = SuspendLockList<Long>()
    private val lastEmits = MutableSharedFlow<T>(
        extraBufferCapacity = 64,// too big!
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun deleteExtraQueueSettings(
        queueId: Long,
    ) {
        lastEmits.tryEmit(
            dataClassDefinitions.createDefault(queueId)
        )
        getFileOf(queueId).delete()
    }

    override suspend fun setExtraQueueSettings(
        extraQueueSettings: T,
    ) {
        require(extraQueueSettings.id >= 0) {
            "queueId must be >= 0. given ${extraQueueSettings.id}"
        }
        val file = getFileOf(extraQueueSettings.id)
        lastEmits.tryEmit(extraQueueSettings)
        return withContext(Dispatchers.IO) {
            updateLocks.withLock(extraQueueSettings.id) {
                transactionalFileSaver.writeObject(
                    file,
                    extraQueueSettings,
                    dataClassDefinitions.serializer,
                )
            }
        }
    }

    override fun getExtraQueueSettings(queueId: Long): T {
        val file = getFileOf(queueId)
        return transactionalFileSaver
            .readObject(file, dataClassDefinitions.serializer)
            ?: dataClassDefinitions.createDefault(queueId)
    }

    override fun getExternalQueueSettingsAsFlow(
        id: Long, initialEmit: Boolean,
    ): Flow<T> {
        return flow {
            if (initialEmit) {
                emit(getExtraQueueSettings(id))
            }
            emitAll(lastEmits.filter { it.id == id })
        }
    }
}

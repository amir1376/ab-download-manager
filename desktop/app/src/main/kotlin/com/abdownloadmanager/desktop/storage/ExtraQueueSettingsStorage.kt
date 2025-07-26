package com.abdownloadmanager.desktop.storage

import ir.amirab.util.desktop.poweraction.ContainsPowerActionConfigOnFinish
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
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
import kotlinx.serialization.Serializable
import java.io.File

class ExtraQueueSettingsStorage(
    private val folder: File,
    private val transactionalFileSaver: TransactionalFileSaver,
) : IExtraQueueSettingsStorage<ExtraQueueSettings> {
    private fun getFileOf(id: Long) = folder
        .resolve("${id}.json")

    private val updateLocks = SuspendLockList(ExtraQueueSettings::id)
    private val lastEmits = MutableSharedFlow<ExtraQueueSettings>(
        extraBufferCapacity = 64,// too big!
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun deleteExtraQueueSettings(
        queueId: Long,
    ) {
        lastEmits.tryEmit(
            ExtraQueueSettings.default(queueId)
        )
        getFileOf(queueId).delete()
    }

    override suspend fun setExtraQueueSettings(
        extraQueueSettings: ExtraQueueSettings,
    ) {
        require(extraQueueSettings.id >= 0) {
            "queueId must be >= 0. given ${extraQueueSettings.id}"
        }
        val file = getFileOf(extraQueueSettings.id)
        lastEmits.tryEmit(extraQueueSettings)
        return withContext(Dispatchers.IO) {
            updateLocks.withLock(extraQueueSettings) {
                transactionalFileSaver.writeObject(
                    file,
                    extraQueueSettings,
                )
            }
        }
    }

    override fun getExtraQueueSettings(queueId: Long): ExtraQueueSettings {
        val file = getFileOf(queueId)
        return transactionalFileSaver
            .readObject(file)
            ?: ExtraQueueSettings.default(queueId)
    }

    override fun getExternalQueueSettingsAsFlow(
        id: Long, initialEmit: Boolean,
    ): Flow<ExtraQueueSettings> {
        return flow {
            if (initialEmit) {
                emit(getExtraQueueSettings(id))
            }
            emitAll(lastEmits.filter { it.id == id })
        }
    }
}


@Serializable
data class ExtraQueueSettings(
    val id: Long,
    val powerActionTypeOnFinish: PowerActionConfig.Type? = null,
    val powerActionUseForceOnFinish: Boolean = false,
) : ContainsPowerActionConfigOnFinish {

    override fun getPowerActionConfigOnFinish() = powerActionTypeOnFinish?.let {
        PowerActionConfig(
            powerActionTypeOnFinish,
            powerActionUseForceOnFinish,
        )
    }

    companion object {
        fun default(
            id: Long
        ) = ExtraQueueSettings(id)
    }
}


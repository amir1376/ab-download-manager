package com.abdownloadmanager.desktop.storage

import ir.amirab.util.desktop.poweraction.ContainsPowerActionConfigOnFinish
import ir.amirab.util.desktop.poweraction.PowerActionConfig
import com.abdownloadmanager.shared.storage.IExtraDownloadSettingsStorage
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

class ExtraDownloadSettingsStorage(
    private val folder: File,
    private val transactionalFileSaver: TransactionalFileSaver,
) : IExtraDownloadSettingsStorage<ExtraDownloadItemSettings> {
    private fun getFileOf(id: Long) = folder
        .resolve("${id}.json")

    private val updateLocks = SuspendLockList(ExtraDownloadItemSettings::id)
    private val lastEmits = MutableSharedFlow<ExtraDownloadItemSettings>(
        extraBufferCapacity = 64,// too big!
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun deleteExtraDownloadItemSettings(
        downloadId: Long,
    ) {
        lastEmits.tryEmit(
            ExtraDownloadItemSettings.default(downloadId)
        )
        getFileOf(downloadId).delete()
    }

    override suspend fun setExtraDownloadItemSettings(
        extraDownloadItemSettings: ExtraDownloadItemSettings,
    ) {
        require(extraDownloadItemSettings.id >= 0) {
            "downloadId must be >= 0"
        }
        val file = getFileOf(extraDownloadItemSettings.id)
        lastEmits.tryEmit(extraDownloadItemSettings)
        return withContext(Dispatchers.IO) {
            updateLocks.withLock(extraDownloadItemSettings) {
                transactionalFileSaver.writeObject(
                    file,
                    extraDownloadItemSettings,
                )
            }
        }
    }

    override fun getExtraDownloadItemSettings(downloadId: Long): ExtraDownloadItemSettings {
        val file = getFileOf(downloadId)
        return transactionalFileSaver
            .readObject(file)
            ?: ExtraDownloadItemSettings.default(downloadId)
    }

    override fun getExternalDownloadItemSettingsAsFlow(
        id: Long, initialEmit: Boolean,
    ): Flow<ExtraDownloadItemSettings> {
        return flow {
            if (initialEmit) {
                emit(getExtraDownloadItemSettings(id))
            }
            emitAll(lastEmits.filter { it.id == id })
        }
    }
}

@Serializable
data class ExtraDownloadItemSettings(
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
        ) = ExtraDownloadItemSettings(id = id)
    }
}

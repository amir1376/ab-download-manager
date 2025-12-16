package com.abdownloadmanager.updateapplier

import com.abdownloadmanager.updatechecker.UpdateSource
import java.io.File

interface UpdatePreparer {
    interface PreparedUpdate {
        fun isValid(): Boolean
    }

    suspend fun prepareUpdate(source: UpdateSource): PreparedUpdate
    suspend fun disposeUpdate(updateSource: UpdateSource)
    suspend fun disposeAllUpdates()
    fun accept(updateSource: UpdateSource): Boolean
}

abstract class UpdateDownloader : UpdatePreparer {
    data class PreparedUpdateFile(
        val file: File
    ) : UpdatePreparer.PreparedUpdate {
        override fun isValid(): Boolean {
            return file.exists()
        }

    }

    override fun accept(updateSource: UpdateSource): Boolean {
        return updateSource is UpdateSource.DirectDownloadLink
    }

    override suspend fun prepareUpdate(source: UpdateSource): UpdatePreparer.PreparedUpdate {
        return PreparedUpdateFile(
            downloadUpdateFile(source as UpdateSource.DirectDownloadLink)
        )
    }

    override suspend fun disposeUpdate(updateSource: UpdateSource) {
        removeUpdateFiles(updateSource as UpdateSource.DirectDownloadLink)
    }

    override suspend fun disposeAllUpdates() {
        return removeAllUpdateFiles()
    }


    abstract suspend fun downloadUpdateFile(updateDirectDownloadLink: UpdateSource.DirectDownloadLink): File
    abstract suspend fun removeUpdateFiles(updateDirectDownloadLink: UpdateSource.DirectDownloadLink)
    abstract suspend fun removeAllUpdateFiles()
}

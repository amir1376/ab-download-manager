package com.abdownloadmanager.updateapplier

import com.abdownloadmanager.updatechecker.UpdateInfo
import com.abdownloadmanager.updatechecker.UpdateSource

abstract class BaseUpdateApplier : UpdateApplier {
    abstract override fun updateSupported(): Boolean

    abstract fun getUpdatePreparer(): UpdatePreparer
    override suspend fun applyUpdate(
        updateInfo: UpdateInfo,
    ) {
        if (!updateSupported()) {
            return
        }
        validateAppStateOnApplyUpdate()
        //it is only check for same instance
        // if I faced to multiple update (when user press "update" many times)
        // I have to cancel this suspension job and create a new instance instead
        if (preparing) {
            return
        }
        preparing = true

        val downloadSource = getBestDownloadSource(updateInfo)
        val updatePreparer = getUpdatePreparer()
        val preparedUpdate = try {
            updatePreparer.prepareUpdate(downloadSource)
        } catch (e: Exception) {
            preparing = false
            throw e
        }
        if (!preparedUpdate.isValid()) {
            preparing = false
            return
        }
        val updateInstaller = getUpdateInstaller(preparedUpdate)
//        updateDownloader.removeUpdate(updateInfo)
        try {
            updateInstaller.installUpdate()
        } catch (e: Exception) {
            throw RuntimeException(
                buildString {
                    appendLine("can't start installation")
                    e.localizedMessage?.let(this::append)
                },
                e,
            )
        }
    }

    protected var preparing: Boolean = false

    protected fun extension(name: String): String {
        return name.substringAfterLast('.', "")
    }

    override suspend fun cleanup() {
        getUpdatePreparer().disposeAllUpdates()
    }

    abstract fun getBestDownloadSource(updateInfo: UpdateInfo): UpdateSource
    abstract fun getUpdateInstaller(preparedUpdate: UpdatePreparer.PreparedUpdate): UpdateInstaller
    open fun validateAppStateOnApplyUpdate() {}

}

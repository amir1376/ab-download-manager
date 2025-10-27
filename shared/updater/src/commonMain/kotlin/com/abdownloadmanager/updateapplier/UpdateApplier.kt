package com.abdownloadmanager.updateapplier

import com.abdownloadmanager.updatechecker.UpdateInfo

interface UpdateApplier {
    fun updateSupported(): Boolean
    suspend fun applyUpdate(updateInfo: UpdateInfo)
    suspend fun cleanup()
}
package com.abdownloadmanager

import com.abdownloadmanager.updateapplier.UpdateApplier
import com.abdownloadmanager.updatechecker.UpdateChecker
import com.abdownloadmanager.updatechecker.UpdateInfo
import ir.amirab.util.AppVersionTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface UpdateCheckStatus {
    data object IDLE : UpdateCheckStatus
    data object NoUpdate : UpdateCheckStatus
    data object NewUpdate : UpdateCheckStatus
    data class Error(val e: Throwable) : UpdateCheckStatus
    data object Checking : UpdateCheckStatus
}

class UpdateManager(
    private val updateChecker: UpdateChecker,
    private val updateApplier: UpdateApplier,
    private val appVersionTracker: AppVersionTracker,
) {
    private var _newVersionData: MutableStateFlow<UpdateInfo?> = MutableStateFlow(null)
    val newVersionData = _newVersionData.asStateFlow()
    private val _updateCheckStatus: MutableStateFlow<UpdateCheckStatus> = MutableStateFlow(UpdateCheckStatus.IDLE)
    val updateCheckStatus = _updateCheckStatus.asStateFlow()
    suspend fun cleanDownloadedFiles() {
        runCatching {
            updateApplier.cleanup()
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun isUpdateSupported(): Boolean {
        return updateApplier.updateSupported()
    }

    suspend fun checkForUpdate(): UpdateInfo? {
        val newUpdateCheck = try {
            _updateCheckStatus.update { UpdateCheckStatus.Checking }
            val checkedData = updateChecker.check()
            _updateCheckStatus.value = if (checkedData == null) {
                UpdateCheckStatus.NoUpdate
            } else {
                UpdateCheckStatus.NewUpdate
            }
            checkedData
        } catch (e: Exception) {
            _updateCheckStatus.update { UpdateCheckStatus.Error(e) }
            null
        }
        _newVersionData.update { newUpdateCheck }
        return newUpdateCheck
    }

    suspend fun update() {
        _newVersionData.value?.let {
            if (updateApplier.updateSupported()) {
                updateApplier.applyUpdate(it)
            }
        }
    }

    // TODO add onAfter update installed
    // ...
}

package com.abdownloadmanager.shared.util.downloaderror.faileddownloads

import com.abdownloadmanager.shared.util.downloaderror.DownloadErrorReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface IFailedDownloadErrorStorage {
    val reasons: StateFlow<Map<Long, DownloadErrorReason>>

    fun setReason(id: Long, reason: DownloadErrorReason)
    fun removeReason(id: Long)
    fun clear()
}

/**
 * this is saved in memory only. this should be enough.
 */
class FailedDownloadErrorStorageInMemory : IFailedDownloadErrorStorage {
    private val _reasons: MutableStateFlow<Map<Long, DownloadErrorReason>> = MutableStateFlow(emptyMap())
    override val reasons = _reasons.asStateFlow()
    override fun setReason(id: Long, reason: DownloadErrorReason) {
        _reasons.update {
            it.plus(id to reason)
        }
    }

    override fun removeReason(id: Long) {
        _reasons.update { it.minus(id) }
    }

    override fun clear() {
        _reasons.update { emptyMap() }
    }
}

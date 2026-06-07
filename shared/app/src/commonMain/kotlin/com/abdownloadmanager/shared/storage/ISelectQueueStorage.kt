package com.abdownloadmanager.shared.storage

import ir.amirab.downloader.queue.DefaultQueueInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

interface ISelectQueueStorage {
    val selectQueueSettings: MutableStateFlow<SelectQueueSettings>
}

@Serializable
data class SelectQueueSettings(
    val queue: Long? = DefaultQueueInfo.ID,
    val startQueue: Boolean = false,
)

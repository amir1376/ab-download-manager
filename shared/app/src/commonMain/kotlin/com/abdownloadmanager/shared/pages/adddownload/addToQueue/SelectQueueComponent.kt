package com.abdownloadmanager.shared.pages.adddownload.addToQueue

import com.abdownloadmanager.shared.util.BaseComponent
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectQueueComponent(
    private val ctx: ComponentContext,
    private val queueManager: QueueManager,
) : BaseComponent(ctx) {
    val queueList = queueManager.queues
    private val _selectedQueue: MutableStateFlow<Long?> = MutableStateFlow(DefaultQueueInfo.ID)
    val selectedQueue = _selectedQueue.asStateFlow()
    fun setSelectedQueue(id: Long?) {
        _selectedQueue.value = id
    }

    private val _startQueue = MutableStateFlow(false)
    val startQueue = _startQueue.asStateFlow()
    fun setStartQueue(value: Boolean) {
        _startQueue.value = value
    }

    private val _rememberThisChoice = MutableStateFlow(false)
    val rememberThisChoice = _rememberThisChoice.asStateFlow()
    fun setRememberThisChoice(value: Boolean) {
        _rememberThisChoice.value = value
    }

    data class OnConfirmParams(
        val queue: Long?,
        val startQueue: Boolean,
    )
}

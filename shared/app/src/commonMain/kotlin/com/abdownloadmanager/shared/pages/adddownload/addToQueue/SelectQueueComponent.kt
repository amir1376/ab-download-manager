package com.abdownloadmanager.shared.pages.adddownload.addToQueue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.abdownloadmanager.shared.storage.ISelectQueueStorage
import com.abdownloadmanager.shared.storage.SelectQueueSettings
import com.abdownloadmanager.shared.util.BaseComponent
import com.arkivanov.decompose.ComponentContext
import ir.amirab.downloader.queue.DefaultQueueInfo
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class SelectQueueComponent(
    private val ctx: ComponentContext,
    private val queueManager: QueueManager,
    private val selectQueueStorage: ISelectQueueStorage,
    private val onRequestAddToQueue: (SelectQueueSettings) -> Unit,
) : BaseComponent(ctx) {
    val queueList = queueManager.queues
    val lastSettings get() = selectQueueStorage.selectQueueSettings.value

    fun loadQueueFromStorage(queue: Long?): Long? {
        queue ?: return null
        return queue.takeIf { q ->
            q >= 0
        }?.takeIf { q ->
            queueList.value.find {
                it.id == q
            } != null
        } ?: DefaultQueueInfo.ID
    }


    private val _selectedQueue: MutableStateFlow<Long?> = MutableStateFlow(
        loadQueueFromStorage(lastSettings.queue)
    )
    val selectedQueue = _selectedQueue.asStateFlow()
    fun setSelectedQueue(id: Long?) {
        _selectedQueue.value = id
    }

    private val _startQueue = MutableStateFlow(lastSettings.startQueue)
    val startQueue = _startQueue.asStateFlow()
    fun setStartQueue(value: Boolean) {
        _startQueue.value = value
    }

    private val _rememberThisChoice = MutableStateFlow(false)
    val rememberThisChoice = _rememberThisChoice.asStateFlow()
    fun setRememberThisChoice(value: Boolean) {
        _rememberThisChoice.value = value
    }

    fun onConfirm() {
        saveSettingsIfNecessary()
        onRequestAddToQueue(
            SelectQueueSettings(
                queue = selectedQueue.value,
                startQueue = _startQueue.value
            )
        )
    }

    fun fastConfirm() {
        onRequestAddToQueue(lastSettings)
    }

    var shouldShowAddToQueue by mutableStateOf(false)
    fun openAddToQueueDialog() {
        shouldShowAddToQueue = true
    }

    fun closeAddToQueue() {
        shouldShowAddToQueue = false
    }

    fun saveSettingsIfNecessary() {
        if (rememberThisChoice.value) {
            selectQueueStorage.selectQueueSettings.value = SelectQueueSettings(
                queue = selectedQueue.value,
                startQueue = startQueue.value,
            )
        }
    }
}

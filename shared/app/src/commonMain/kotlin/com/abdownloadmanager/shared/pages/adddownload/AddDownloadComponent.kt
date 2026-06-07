package com.abdownloadmanager.shared.pages.adddownload

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.abdownloadmanager.shared.pagemanager.CategoryDialogManager
import com.abdownloadmanager.shared.pages.adddownload.addToQueue.SelectQueueComponent
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.storage.ISelectQueueStorage
import com.abdownloadmanager.shared.util.BaseComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ir.amirab.downloader.queue.QueueManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class AddDownloadComponent(
    ctx: ComponentContext,
    val id: String,
    lastSavedLocationsStorage: ILastSavedLocationsStorage,
    protected val queueManager: QueueManager,
    private val selectQueueStorage: ISelectQueueStorage,
) : BaseComponent(ctx) {
    companion object {
        const val lastLocationsCacheSize = 4
    }

    abstract fun getCategoryPageManager(): CategoryDialogManager
    fun onRequestAddCategory() {
        getCategoryPageManager().openCategoryDialog(-1)
    }

    private var dialogUsed = false
    protected fun consumeDialog(block: () -> Unit) {
        if (dialogUsed) {
            return
        }
        block()
        dialogUsed = true
    }

    private val _lastUsedLocations = lastSavedLocationsStorage.lastUsedSaveLocations
    val lastUsedLocations: StateFlow<List<String>> = _lastUsedLocations.asStateFlow()
    fun addToLastUsedLocations(saveLocation: String) {
        _lastUsedLocations.update {
            buildList {
                add(saveLocation)
                addAll(it)
            }
                .distinct()
                .take(lastLocationsCacheSize)
        }
    }

    fun removeFromLastDownloadLocation(saveLocation: String) {
        _lastUsedLocations.update {
            it.filter { it != saveLocation }
        }
    }

    abstract fun onRequestAddToQueue(
        queueId: Long?,
        startQueue: Boolean,
    )

    val selectQueueComponent = SelectQueueComponent(
        ctx = childContext("showAddToQueueComponent"),
        queueManager = queueManager,
        selectQueueStorage = selectQueueStorage,
        onRequestAddToQueue = {
            onRequestAddToQueue(it.queue, it.startQueue)
        }
    )

    abstract val shouldShowWindow: StateFlow<Boolean>
}

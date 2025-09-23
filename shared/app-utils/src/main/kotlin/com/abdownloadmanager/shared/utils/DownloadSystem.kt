package com.abdownloadmanager.shared.utils

import com.abdownloadmanager.shared.storage.IExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import com.abdownloadmanager.shared.utils.category.CategoryItemWithId
import com.abdownloadmanager.shared.utils.category.CategoryManager
import com.abdownloadmanager.shared.utils.category.CategorySelectionMode
import com.abdownloadmanager.shared.utils.ondownloadcompletion.OnDownloadCompletionActionRunner
import com.abdownloadmanager.shared.utils.onqueuecompletion.OnQueueEventActionRunner
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.db.IDownloadListDb
import ir.amirab.downloader.downloaditem.*
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.StoppedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.downloaditem.DownloadStatus
import ir.amirab.downloader.monitor.IDownloadItemState
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.downloader.monitor.ProcessingDownloadItemState
import ir.amirab.downloader.monitor.isDownloadActiveFlow
import ir.amirab.downloader.queue.QueueManager
import ir.amirab.downloader.utils.OnDuplicateStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * a facade for download manager library
 * all the download manager features should be accessed and controlled here
 */
class DownloadSystem(
    val downloadManager: DownloadManager,
    val queueManager: QueueManager,
    val categoryManager: CategoryManager,
    val downloadMonitor: IDownloadMonitor,
    val onDownloadCompletionActionRunner: OnDownloadCompletionActionRunner,
    val onQueueEventActionRunner: OnQueueEventActionRunner,
    private val scope: CoroutineScope,
    private val downloadListDB: IDownloadListDb,
    private val extraQueueSettingsStorage: IExtraQueueSettingsStorage<*>,
    private val extraDownloadSettingsStorage: IExtraDownloadSettingsStorage<*>,
    private val foldersRegistry: DownloadFoldersRegistry,
) {
    private val booted = MutableStateFlow(false)

    val downloadEvents = downloadManager.listOfJobsEvents

    suspend fun boot() {
        if (booted.value) return
        foldersRegistry.boot()
        queueManager.boot()
        downloadManager.boot()
        categoryManager.boot()
        onDownloadCompletionActionRunner.startListening()
        onQueueEventActionRunner.startListening()
        booted.update { true }
    }

    suspend fun addDownload(
        newItemsToAdd: List<IDownloadItem>,
        onDuplicateStrategy: (IDownloadItem) -> OnDuplicateStrategy,
        queueId: Long? = null,
        categorySelectionMode: CategorySelectionMode? = null,
    ): List<Long> {
        val createdIds = newItemsToAdd.map {
            downloadManager.addDownload(it, onDuplicateStrategy(it))
        }
        createdIds.also { ids ->
            queueId?.let {
                queueManager.addToQueue(
                    it, ids
                )
            }
        }
        categorySelectionMode?.let {
            when (it) {
                CategorySelectionMode.Auto -> {
                    categoryManager.autoAddItemsToCategoriesBasedOnFileNames(
                        createdIds.mapIndexed { index: Int, id: Long ->
                            val downloadItem = newItemsToAdd[index]
                            CategoryItemWithId(
                                id = id,
                                fileName = downloadItem.name,
                                url = downloadItem.link,
                            )
                        }
                    )
                }

                is CategorySelectionMode.Fixed -> {
                    categoryManager.addItemsToCategory(
                        it.categoryId,
                        createdIds,
                    )
                }
            }
        }
        return createdIds
    }

    suspend fun addDownload(
        downloadItem: IDownloadItem,
        onDuplicateStrategy: OnDuplicateStrategy,
        queueId: Long?,
        categoryId: Long?,
        context: DownloadItemContext = EmptyContext,
    ): Long {
        val downloadId = downloadManager.addDownload(downloadItem, onDuplicateStrategy, context)
        queueId?.let {
            queueManager.addToQueue(queueId, downloadId)
        }
        categoryId?.let {
            categoryManager.addItemsToCategory(
                categoryId = categoryId,
                itemIds = listOf(downloadId)
            )
        }
        return downloadId
    }

    suspend fun removeDownload(
        id: Long,
        alsoRemoveFile: Boolean,
        context: DownloadItemContext,
    ) {
        downloadManager.deleteDownload(
            id = id,
            alsoRemoveFile = {
                alsoRemoveFile
            },
            context = context
        )
        categoryManager.removeItemInCategories(listOf(id))
        extraDownloadSettingsStorage.deleteExtraDownloadItemSettings(id)
    }

    suspend fun manualResume(id: Long): Boolean {
        manualResume(id, ResumedBy(User))
        return true
    }

    suspend fun manualResume(id: Long, context: DownloadItemContext): Boolean {
        downloadManager.resume(id, context)
        return true
    }

    suspend fun reset(id: Long): Boolean {
        downloadManager.reset(id)
        return true
    }

    suspend fun manualPause(id: Long): Boolean {
//        if (mainDownloadQueue.isQueueActive) {
//            return false
//        }
        downloadManager.pause(id, StoppedBy(User))
        return true
    }

    suspend fun startQueue(
        queueId: Long,
    ) {
        val queue = queueManager.getQueue(queueId)
        if (queue.isQueueActive) {
            return
        }
//      going to start
        queue.start()
    }

    suspend fun stopAnything() {
        queueManager.getAll().forEach {
            it.stop()
        }
        downloadManager.stopAll()
    }

    suspend fun stopQueue(
        queueId: Long,
    ) {
        queueManager.getQueue(queueId)
            .stop()
    }

    suspend fun getDownloadItemById(id: Long): IDownloadItem? {
        return downloadListDB.getById(id) ?: return null
    }

    suspend fun getDownloadItemByLink(link: String): List<IDownloadItem> {
        return downloadListDB.getAll().filter {
            it.link == link
        }
    }

    suspend fun getDownloadItemsBy(selector: (IDownloadItem) -> Boolean): List<IDownloadItem> {
        return downloadListDB.getAll().filter(selector)
    }

    suspend fun getOrCreateDownloadByLink(
        downloadItem: IDownloadItem,
    ): Long {
        val items = getDownloadItemByLink(downloadItem.link)
        if (items.isNotEmpty()) {
            val completedFound = items.find { it.status == DownloadStatus.Completed }
            if (completedFound != null) {
                return completedFound.id
            }
            val id = items.sortedByDescending { it.dateAdded }.first().id
            return id
        }
        val id = addDownload(
            downloadItem = downloadItem,
            onDuplicateStrategy = OnDuplicateStrategy.AddNumbered,
            queueId = null,
            categoryId = null,
        )
        return id
    }

    fun getDownloadFile(downloadItem: IDownloadItem): File {
        return downloadManager.calculateOutputFile(downloadItem)
    }

    fun getDownloadItemByPath(path: String): IDownloadItemState? {
        return downloadMonitor.downloadListFlow.value.find {
            it.getFullPath().path == path
        }
    }

    fun getDownloadItemsByFolder(folder: String): List<IDownloadItemState> {
        return downloadMonitor.downloadListFlow.value.filter {
            it.folder == folder
        }
    }


    suspend fun getFilePathById(id: Long): File? {
        val item = getDownloadItemById(id) ?: return null
        return downloadManager.calculateOutputFile(item)
    }

    fun addQueue(name: String) {
        scope.launch {
            queueManager.addQueue(name)
        }
    }

    fun getAllDownloadIds(): List<Long> {
        return getUnfinishedDownloadIds() + getFinishedDownloadIds()
    }

    fun getFinishedDownloadIds(): List<Long> {
        return downloadMonitor.completedDownloadListFlow.value.map {
            it.id
        }
    }

    fun getUnfinishedDownloadIds(): List<Long> {
        return downloadMonitor.activeDownloadListFlow.value.map {
            it.id
        }
    }

    fun isDownloadMissingFileOrHaveNotProgress(downloadItem: IDownloadItemState): Boolean {
        val missingFileBypass = if (downloadItem is ProcessingDownloadItemState) {
            // some downloads not started yet so there is no file belong to them, so we shouldn't remove them
            downloadItem.hasProgress
        } else {
            // finished downloads can be removed
            true
        }
        return missingFileBypass && !downloadItem.getFullPath().exists()
    }

    fun getListOfDownloadThatMissingFileOrHaveNotProgress(): List<IDownloadItemState> {
        val downloads = downloadMonitor.downloadListFlow.value
        return downloads.filter {
            isDownloadMissingFileOrHaveNotProgress(it)
        }
    }

    fun getAllRegisteredDownloadFiles(): List<File> {
        return downloadMonitor.run {
            activeDownloadListFlow.value + completedDownloadListFlow.value
        }.map {
            File(it.folder, it.name)
        }
    }

    suspend fun isDownloadActive(id: Long): Boolean {
        return downloadMonitor.isDownloadActiveFlow(id).value
    }

    suspend fun editDownload(id: Long, applyUpdate: (IDownloadItem) -> Unit) {
        val wasActive = isDownloadActive(id)
        if (wasActive) {
            manualPause(id)
        }
        downloadManager.updateDownloadItem(id, applyUpdate)
        if (wasActive) {
            manualResume(id)
        }
    }

    suspend fun deleteQueue(queueId: Long) {
        queueManager.deleteQueue(queueId)
        extraQueueSettingsStorage.deleteExtraQueueSettings(queueId)
    }
}

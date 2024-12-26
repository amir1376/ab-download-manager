package com.abdownloadmanager.utils

import com.abdownloadmanager.utils.category.CategoryItemWithId
import com.abdownloadmanager.utils.category.CategoryManager
import com.abdownloadmanager.utils.category.CategorySelectionMode
import ir.amirab.downloader.DownloadManager
import ir.amirab.downloader.db.IDownloadListDb
import ir.amirab.downloader.downloaditem.*
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.StoppedBy
import ir.amirab.downloader.downloaditem.contexts.User
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
    private val scope: CoroutineScope,
    private val downloadListDB: IDownloadListDb,
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
        booted.update { true }
    }

    suspend fun addDownload(
        newItemsToAdd: List<DownloadItem>,
        onDuplicateStrategy: (DownloadItem) -> OnDuplicateStrategy,
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
        downloadItem: DownloadItem,
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
        downloadManager.deleteDownload(id, {
            if (it.status == DownloadStatus.Completed) {
                alsoRemoveFile
            } else {
                // always remove file if download is not finished!
                true
            }
        }, context)
        categoryManager.removeItemInCategories(listOf(id))
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

    suspend fun getDownloadItemById(id: Long): DownloadItem? {
        return downloadListDB.getById(id) ?: return null
    }

    suspend fun getDownloadItemByLink(link: String): List<DownloadItem> {
        return downloadListDB.getAll().filter {
            it.link == link
        }
    }

    suspend fun getOrCreateDownloadByLink(
        downloadItem: DownloadItem,
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

    fun getDownloadFile(downloadItem: DownloadItem): File {
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

    suspend fun editDownload(updatedItem: DownloadItem) {
        val wasActive = isDownloadActive(updatedItem.id)
        if (wasActive) {
            manualPause(updatedItem.id)
        }
        downloadManager.updateDownloadItem(updatedItem.id) { currentItem ->
            var shouldUpdate = true
            if (currentItem.folder == updatedItem.folder && currentItem.name != updatedItem.name) {
                val success = getDownloadFile(currentItem).renameTo(getDownloadFile(updatedItem))
                shouldUpdate = success
            }
            if (shouldUpdate) {
                currentItem.applyFrom(updatedItem)
            }
        }
        if (wasActive) {
            manualResume(updatedItem.id)
        }
    }
}
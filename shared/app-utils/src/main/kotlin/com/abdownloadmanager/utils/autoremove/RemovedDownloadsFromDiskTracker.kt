package com.abdownloadmanager.utils.autoremove

import com.abdownloadmanager.utils.DownloadSystem
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.irgaly.kfswatch.KfsEvent
import ir.amirab.downloader.downloaditem.contexts.CanPerformRemove
import ir.amirab.downloader.downloaditem.contexts.RemovedBy
import ir.amirab.downloader.monitor.*
import ir.amirab.util.flow.withPrevious
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File


class RemovedDownloadsFromDiskTracker(
    private val downloadMonitor: IDownloadMonitor,
    private val scope: CoroutineScope,
    private val downloadSystem: DownloadSystem,
) {
    private fun createWatcher(
        scope: CoroutineScope,
    ): KfsDirectoryWatcher {
        return KfsDirectoryWatcher(
            scope = scope,
        )
    }

    @Volatile
    private var stopped = true

    //download item ids that should be checked for existence after a delay
    private var itemsToCheck = MutableStateFlow(emptySet<Long>())
    private var activeJob: Job? = null

    fun start() {
        stopped = false
        activeJob = scope.launch {
            val watcher = createWatcher(this)
            watcher
                .onEventFlow
                .filter { it.event == KfsEvent.Delete }
                .onEach {
                    val fullPath = File(it.targetDirectory, it.path).path
                    onPathRemoved(fullPath)
                }
                .launchIn(this)
            downloadMonitor.downloadListFlow
                .map { it.map { it.folder }.distinct() }
                .distinctUntilChanged()
                .changes()
                .onEach { changes ->
                    val groups = changes
                        .groupBy { it.second }
                    groups[Change.Removed]
                        ?.takeIf { it.isNotEmpty() }
                        ?.map { it.first }?.toTypedArray()?.let {
                            watcher.remove(*it)
                        }
                    groups[Change.Added]
                        ?.takeIf { it.isNotEmpty() }
                        ?.map { it.first }?.toTypedArray()?.let {
                            watcher.add(*it)
                        }
                }
                .launchIn(this)
            itemsToCheck
                .debounce(500)
                .filter { it.isNotEmpty() }
                .onEach { downloadItems ->
                    checkAndRemoveThisItems(downloadItems)
                    itemsToCheck.update { it.subtract(downloadItems) }
                }.launchIn(this)
        }
    }

    suspend fun stop() {
        activeJob?.cancelAndJoin()
        activeJob = null
        itemsToCheck.update { emptySet() }
        stopped = true
    }

    suspend fun removeDownloadsThatFilesAreMissing() {
        checkAndRemoveThisItems(
            downloadSystem.getListOfDownloadThatMissingFileOrHaveNotProgress()
                .map { it.id }
                .toSet()
        )
    }

    private suspend fun checkAndRemoveThisItems(ids: Set<Long>) {
        for (id in ids) {
            val downloadItem = downloadSystem.getDownloadItemById(id) ?: continue
            val file = downloadSystem.getDownloadFile(downloadItem)
            if (!file.exists()) {
                downloadSystem.removeDownload(
                    id = downloadItem.id,
                    alsoRemoveFile = false, // it is already deleted!
                    context = RemovedBy(AutoRemoveOption)
                )
            }
        }
    }

    /**
     * find the corespounding download and schedule for  remove that
     * I will add a delay for that (maybe it's a temporary file remove for example when renaming download item)
     */
    private fun onPathRemoved(path: String) {
        if (stopped) return
        val item = downloadSystem.getDownloadItemByPath(path) ?: return
        itemsToCheck.update { it.plus(item.id) }
    }
}

private sealed interface Change {
    data object Added : Change
    data object Removed : Change
    data object NotChange : Change
}


private fun <T> Flow<List<T>>.changes(): Flow<List<Pair<T, Change>>> {
    return withPrevious { previous, current ->
        if (previous == null) {
            current.map { it to Change.Added }
        } else {
            diffOf(previous, current)
        }
    }
}

private fun <T> diffOf(
    a: Collection<T>, b: Collection<T>,
): List<Pair<T, Change>> {
    val output = ArrayList<Pair<T, Change>>(maxOf(a.size, b.size))
    val aSet = a.toSet()
    val remainingBItems = b.toMutableSet()
    // find removed items in b
    for (i in aSet) {
        if (i in remainingBItems) {
            output.add(i to Change.NotChange)
            remainingBItems.remove(i)
        } else {
            output.add(i to Change.Removed)
        }
    }
    //  remaining b's are added!
    output.addAll(remainingBItems.map { it to Change.Added })
    return output
}

data object AutoRemoveOption : CanPerformRemove

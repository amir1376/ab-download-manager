package ir.amirab.downloader.queue

import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.StoppedBy
import ir.amirab.downloader.downloaditem.contexts.User
import ir.amirab.downloader.utils.swap
import ir.amirab.downloader.utils.swapped
import ir.amirab.util.coroutines.debounce
import ir.amirab.util.guardedEntry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Collections

/**
 * this queue is used to limit global concurrent download limit
 */

class ManualDownloadQueue(
    private val downloadEvents: DownloadManagerMinimalControl,
    private val scope: CoroutineScope,
) {

    private var booted = guardedEntry()

    // how many downloads can be active at the same time
    // 0 or fewer means unlimited!
    // this is protected with a set method
    private var maxConcurrent = Int.MAX_VALUE

    // downloads that are currently active
    private val activeItemsFlow: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList())

    // just a shortcut
    private val activeItems get() = activeItemsFlow.value

    // this is called from the pause function which can be called from any thread
    // so we should make it thread safe
    private val trimmedItems = Collections.synchronizedCollection(mutableSetOf<Long>())
    private var totalItemsFlow: MutableStateFlow<List<Long>> = MutableStateFlow(emptyList())
    val pendingItems = combine(
        activeItemsFlow.map { it.toSet() },
        totalItemsFlow,
    ) { active, total ->
        total - active
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())


    fun boot() {
        booted.action {
            startListenerJob()
        }
    }

    private fun removeActiveItem(id: Long): Boolean {
        var removed = false
        activeItemsFlow.update {
            val size = it.size
            val newList = it - id
            if (size != newList.size) {
                removed = true
            }
            newList
        }
        return removed
    }

    private fun addAnActiveItem(id: Long) {
        return activeItemsFlow.update {
            (it + id).distinct()
        }
    }

    private fun clearActiveItems() {
        activeItemsFlow.update { emptyList() }
    }

    private fun clearTotalItems() {
        totalItemsFlow.update { emptyList() }
    }

    private fun onDownloadCanceled(id: Long, e: Throwable) {
        if (trimmedItems.remove(id)) {
            // it was trimmed so we shouldn't remove it from queue
            // it should be processed after
            removeActiveItem(id)
        } else {
            removeFromQueue(id)
        }
        shake()
    }


    private fun onDownloadFinished(id: Long) {
        removeFromQueue(id)
        shake()
    }

    private fun ensureBooted() {
        if (booted.isDone()) {
            error("headless queue is not booted!")
        }
    }

    private fun actualShake(): Boolean {
        val activeItems = activeItems
        val maxConcurrent = maxConcurrent
        if (activeItems.size < maxConcurrent) {
            extend()
        } else if (activeItems.size > maxConcurrent) {
            trim()
        }
        return true
    }

    // If multiple downloads are canceled at the same time, multiple `shake` calls may occur.
    // In these situations, there is an issue where other downloads might resume
    // (even though they are already being stopped but their events have not been received yet).
    // So we use a debounced call to ensure all events are received first.
    private val shakeDebounce = scope.debounce(
        ::actualShake,
        500, // this should be enough even for slow devices
    )

    private fun shake(delayed: Boolean = true) {
        if (delayed) {
            shakeDebounce()
        } else {
            actualShake()
        }
    }

    private var listenerJob: Job? = null
    private fun startListenerJob() {
        listenerJob = downloadEvents.listOfJobsEvents.onEach {
            if (!totalItemsFlow.value.contains(it.downloadItem.id)) {
                //skip this event
                return@onEach
            }
            when (it) {
                is DownloadManagerEvents.OnJobAdded -> {
                }

                is DownloadManagerEvents.OnJobCanceled -> onDownloadCanceled(
                    it.downloadItem.id,
                    it.e
                )

                is DownloadManagerEvents.OnJobCompleted -> onDownloadFinished(it.downloadItem.id)
                is DownloadManagerEvents.OnJobStarted -> {}

                is DownloadManagerEvents.OnJobStarting -> {
                    if (it.context[ResumedBy]?.by != me) {
                        // someone else resumed the download
                        // we just remove it
                        removeFromQueue(it.downloadItem.id)
                    }
                }

                is DownloadManagerEvents.OnJobChanged -> {}
                is DownloadManagerEvents.OnJobRemoved -> onDownloadRemoved(it.downloadItem.id)
            }
        }.launchIn(scope)
    }

    private fun onDownloadRemoved(id: Long) {
        removeFromQueue(id)
    }


    fun setMaxConcurrent(value: Int) {
        maxConcurrent = if (value <= 0) {
            Int.MAX_VALUE
        } else {
            value
        }
        shake()
    }

    fun move(listOfIds: List<Long>, diff: Int) {
        if (diff == 0) return
        totalItemsFlow.update { queueItems ->
            val movingIndexes = listOfIds.mapNotNull {
                queueItems.indexOf(it).takeIf { index -> index != -1 }
            }
                //from big to small
                .sortedDescending()
                .let {
                    if (diff < 0) it.reversed()
                    else it
                }
            if (movingIndexes.isEmpty()) {
                return@update queueItems
            }
            val m = queueItems.toMutableList()
            val queueIndices = queueItems.indices
            val dontMovedPositions = mutableSetOf<Int>()
//            println("moving indexes $movingIndexes")
            for (index in movingIndexes) {
                val newPosition = index + diff
                //don't move out of list index
                if (newPosition !in queueIndices) {
//                    println("we don't move index $index to $newPosition")
                    dontMovedPositions.add(index)
                    continue
                }
                //we don't want to swap to an item that already wants swap
                if (newPosition in dontMovedPositions) {
//                    println("we don't move index $index to $newPosition cause of $dontMovedPositions")
                    dontMovedPositions.add(index)
                    continue
                }
                m.swap(index, newPosition)
            }
            m.toList()
        }
    }

    fun moveUp(listOfIds: List<Long>) {
        move(listOfIds, -1)
    }

    fun moveDown(listOfIds: List<Long>) {
        move(listOfIds, 1)
    }

    fun swapOrders(order: Int, toOrder: (List<Long>) -> Int) {
        totalItemsFlow.update { items ->
            items.swapped(order, toOrder(items))
        }
    }

    private fun trim() {
        while (activeItems.size > maxConcurrent) {
            if (!removeAnActiveQueueItemForTrimming()) {
                return
            }
        }
    }

    private val me = User
    private fun removeAnActiveQueueItemForTrimming(): Boolean {
        val id = activeItems.lastOrNull() ?: return false
        trimmedItems.add(id)
        val result = removeActiveItem(id)
        scope.launch { downloadEvents.stopJob(id, StoppedBy(me)) }
        return result
    }

    private fun extend() {
        while (maxConcurrent > activeItems.size) {
            val queueItemStarted = downloadAQueueItemIfPossible()
            if (!queueItemStarted) {
                return
            }
        }
    }

    /**
     * @return is any item from queue started ?
     */
    private fun downloadAQueueItemIfPossible(): Boolean {
        val downloadableItemFromQueue = getAnInactiveITemFromTheQueue()

        return downloadableItemFromQueue?.let {
            addAnActiveItem(it)
            scope.launch {
                downloadEvents.startJob(it, ResumedBy(me))
            }
            true
        } ?: false
    }

    private fun getAnInactiveITemFromTheQueue(): Long? {
        while (true) {
            val activeItems = activeItems
            val item = totalItemsFlow.value
                .firstOrNull { it !in activeItems }
            if (item == null) {
                // no item returning now!
                return null
            }
            if (!downloadEvents.canActivateJob(item)) {
                // finished or in status that we can't use it anymore
                // remove it!
                removeFromQueue(item)
                continue
            }
            return item
        }
    }


    fun getOrder(item: Long): Int {
        return totalItemsFlow.value.indexOf(item)
    }

    fun getQueueItemFromOrder(order: Int): Long? {
        return totalItemsFlow.value.getOrNull(order)
    }

    fun clearQueue() {
        trimmedItems.clear()
        clearActiveItems()
        clearTotalItems()
    }

    fun removeFromQueue(ids: Set<Long>) {
        totalItemsFlow.update {
            it.filter { id ->
                id !in ids
            }
        }
        activeItemsFlow.update {
            it.filter {
                it !in ids
            }
        }
        trimmedItems.removeAll(ids)
    }

    fun removeFromQueue(id: Long) {
        return removeFromQueue(setOf(id))
    }

    fun resume(item: Long) {
        totalItemsFlow.update {
            it + item
        }
        shake(
            // immediately shake the queue
            delayed = false
        )
    }

    // called by user
    suspend fun pause(item: Long) {
        // this should be removed here as it indicates that user manually paused it
        // so the trimmed item list should not contain it anymore
        trimmedItems.remove(item)
        downloadEvents.stopJob(item, StoppedBy(me))
    }
}

package ir.amirab.downloader.queue

import ir.amirab.downloader.DownloadManagerEvents
import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.downloader.db.DownloadQueuePersistedDataAccess
import ir.amirab.downloader.db.QueueModel
import ir.amirab.downloader.downloaditem.contexts.Queue
import ir.amirab.downloader.downloaditem.contexts.ResumedBy
import ir.amirab.downloader.downloaditem.contexts.StoppedBy
import ir.amirab.downloader.utils.swap
import ir.amirab.downloader.utils.swapped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


class DownloadQueue(
    persistedModel: QueueModel,
    val persistedData: DownloadQueuePersistedDataAccess,
    val downloadEvents: DownloadManagerMinimalControl,
    val onQueueEvent: (QueueEvent) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob())

    //    private val mutex = Mutex()
    private var booted = false

    private val _queueModel = MutableStateFlow(
        persistedModel
    )
    val queueModel = _queueModel.asStateFlow()
    fun getQueueModel() = queueModel.value

    // this must not change
    val id: Long = getQueueModel().id
    private val stopQueueOnEmpty: Boolean
        get() = getQueueModel().stopQueueOnEmpty
    private val maxConcurrent
        get() = getQueueModel().maxConcurrent
    private val scheduleTimes: ScheduleTimes
        get() = getQueueModel().scheduledTimes

    private val activeItems = mutableSetOf<Long>()
    private val canceledItems = mutableSetOf<Long>()
    private val trimmedItems = mutableSetOf<Long>()

    private val _queueActiveFlow = MutableStateFlow(false)
    val activeFlow = _queueActiveFlow.asStateFlow()
    val isQueueActive: Boolean get() = activeFlow.value

    fun onEvent(event: QueueEvent) {
        this.onQueueEvent(event)
    }

    suspend fun boot() {
        if (booted) {
            return
        }
        startListenerJob()
        setupAutoStartAndStop()
        setupAutoSave()
        booted = true
    }

    private fun setupAutoSave() {
        queueModel.onEach {
            persist()
        }.launchIn(scope)
    }

    private fun setupAutoStartAndStop() {
        setUpAutoStartJob()
        setUpAutoStopJob()
    }

    private fun setActive(v: Boolean) {
        _queueActiveFlow.value = v
        if (v) {
//            println("start queue job")
        } else {
//            println("stop queue job")
        }
    }

    private suspend fun onDownloadCanceled(id: Long, e: Throwable) {
        val removed = activeItems.remove(id)
        if (isQueueActive) {
            if (!trimmedItems.remove(id)){
                canceledItems.add(id)
            }
            swapQueueItem(
                item = id,
                //I make it function because of StateFlow::update
                toPosition = { q ->
                    q.lastIndex
                })
        }
        shake(
            itemChangeHappened = removed,
        )
    }


    private fun onDownloadFinished(id: Long) {
        removeFromQueue(id)
        shake(
            itemChangeHappened = true,
        )
    }

    suspend fun start(): Boolean {
        if (stopping) return false
//        println("on start queue")
        canceledItems.clear()
        trimmedItems.clear()
        ensureBooted()
        setActive(true)
//        println("starting")
        return shake()
    }

    private fun ensureBooted() {
        if (!booted) {
            error("queue is not booted!")
        }
    }

    fun shake(
        itemChangeHappened: Boolean = false
    ): Boolean {
//        println("shake queue")
        return when {
            !isQueueActive -> false
            activeItems.isEmpty() && (getDownloadableItemFromQueue() == null) -> {
                if (stopQueueOnEmpty) {
                    stop()
                }
                if (itemChangeHappened) {
                    onEvent(QueueEvent.OnQueueBecomesEmpty(id))
                }
                false
            }

            else -> {
                if (activeItems.size < maxConcurrent) {
                    extend()
                } else if (activeItems.size > maxConcurrent) {
                    trim()
                }
                true
            }
        }
    }

    private var listenerJob: Job? = null
    private fun startListenerJob() {
        listenerJob = downloadEvents.listOfJobsEvents.onEach {
            if (!getQueueModel().queueItems.contains(it.downloadItem.id)) {
                //skip this event
                return@onEach
            }
//            println("we (${getQueueModel().name}) found ${it.downloadItem.id} in ${getQueueModel().queueItems} command ${it}")
            when (it) {
                is DownloadManagerEvents.OnJobAdded -> {
                }

                is DownloadManagerEvents.OnJobCanceled -> onDownloadCanceled(
                    it.downloadItem.id,
                    it.e
                )

                is DownloadManagerEvents.OnJobCompleted -> onDownloadFinished(it.downloadItem.id)
                is DownloadManagerEvents.OnJobStarted -> {}
                is DownloadManagerEvents.OnJobStarting -> {}
                is DownloadManagerEvents.OnJobChanged -> {}
                is DownloadManagerEvents.OnJobRemoved -> onDownloadRemoved(it.downloadItem.id)
            }
        }.launchIn(scope)
    }

    private var autoStartJob: Job? = null
    private fun cancelAutoStartJob() {
        autoStartJob?.cancel()
        autoStartJob = null
    }

    private fun setUpAutoStartJob() {
        cancelAutoStartJob()
        val scheduleTimes = scheduleTimes
        if (scheduleTimes.enabledStartTime) {
            autoStartJob = scope.launch {
                delay(scheduleTimes.getNearestTimeToStart())
                val wasActive = isQueueActive
                onEvent(QueueEvent.OnQueueStartTimeReached(id, wasActive))
                start()
                //wait a little
                delay(1000)
                //for tomorrow
                setUpAutoStartJob()
            }
        }
    }

    private var autoStopJob: Job? = null
    private fun cancelAutoStopJob() {
        autoStopJob?.cancel()
        autoStopJob = null
    }

    private fun setUpAutoStopJob() {
        cancelAutoStopJob()
        val scheduleTimes = scheduleTimes
        if (scheduleTimes.enabledEndTime) {
            autoStopJob = scope.launch {
                delay(scheduleTimes.getNearestTimeToStop())
                val wasActive = isQueueActive
                onEvent(QueueEvent.QueueEndTimeReached(id, wasActive))
                stop()
                //wait a little
                delay(1000)
                //for tomorrow
                setUpAutoStopJob()
            }
        }
    }


    private fun onDownloadRemoved(id: Long) {
        removeFromQueue(id)
    }

    fun stop() {
        scope.launch {
//            println("stopping")
            stopAsync()
        }
    }

    @Volatile
    var stopping = false
    suspend fun stopAsync() {
        if (stopping) return
        setActive(false)
        stopping = true
        //active item is a synchronized list so we should iterate over it FAST!
        val stopJobs = activeItems.map {
            scope.async {
                downloadEvents.stopJob(it,StoppedBy(me))
            }
        }
        kotlin.runCatching {
            stopJobs.awaitAll()
        }.onFailure {
            // should not happen!
//            it.printStackTrace()
        }
        stopping = false
    }


    fun setScheduledTimes(
        updater: ScheduleTimes.() -> ScheduleTimes
    ) {
        _queueModel.update {
            it.copy(scheduledTimes = updater(it.scheduledTimes))
        }
        setupAutoStartAndStop()
    }

    fun setName(newValue: String) {
        _queueModel.update {
            it.copy(name = newValue)
        }
    }

    fun setMaxConcurrent(value: Int) {
        _queueModel.update {
            it.copy(maxConcurrent = value)
        }
        shake()
    }

    fun setStopQueueOnEmpty(enabled: Boolean) {
        _queueModel.update {
            it.copy(
                stopQueueOnEmpty = enabled
            )
        }
        shake()
    }

    fun move(listOfIds: List<Long>, diff: Int) {
        if (diff == 0) return
        _queueModel.update { q ->
            val movingIndexes = listOfIds.mapNotNull {
                q.queueItems.indexOf(it).takeIf { index -> index != null }
            }
                //from big to small
                .sortedDescending()
                .let {
                    if (diff < 0) it.reversed()
                    else it
                }
            if (movingIndexes.isEmpty()) {
                return@update q
            }
            val m = q.queueItems.toMutableList()
            val queueIndices = q.queueItems.indices
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
            q.copy(
                queueItems = m.toList()
            )
        }
    }

    fun moveUp(listOfIds: List<Long>) {
        move(listOfIds, -1)
    }

    fun moveDown(listOfIds: List<Long>) {
        move(listOfIds, 1)
    }

    fun swapOrders(order: Int, toOrder: (List<Long>) -> Int) {
        _queueModel.update {
            it.copy(
                queueItems = it.queueItems.swapped(order, toOrder(it.queueItems))
            )
        }
    }

    fun swapQueueItem(item: Long, toPosition: (List<Long>) -> Int) {
        _queueModel.update {
            val q = it.queueItems
            val currentIndex = q.indexOf(item).takeIf { it >= 0 }
            if (currentIndex == null) {
                it
            } else {
                val swapToThisPosition = toPosition(q)
                val modifiedItems = q
                    .swapped(currentIndex, swapToThisPosition)
                it.copy(
                    queueItems = modifiedItems
                )
            }

        }
//        println("going to swipe $currentIndex , ${queue.size}")
    }


    private fun trim() {
        val count = activeItems.size - maxConcurrent
        repeat(count) {
            if (!removeAnActiveQueueItem()) {
                return
            }
        }
    }
    private val me by lazy { Queue(id) }
    private fun removeAnActiveQueueItem(): Boolean {
        val id = activeItems.lastOrNull() ?: return false
        trimmedItems.add(id)
        val result = activeItems.remove(id)
        scope.launch { downloadEvents.stopJob(id,StoppedBy(me)) }
        return result
    }

    private fun extend() {
        repeat(maxConcurrent - activeItems.size) {
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
        return getDownloadableItemFromQueue()?.let {
            activeItems.add(it)
            scope.launch {
                downloadEvents.startJob(it,ResumedBy(me))
            }
            true
        } ?: false
    }

    private fun getDownloadableItemFromQueue(): Long? {
//        println(queue)
        return getQueueModel().queueItems.firstOrNull {
            when {
                it in activeItems -> {
//                    println("it is not in active items")
                    false
                }

                it in canceledItems -> {
//                    println("it is in canceled items")
                    false
                }

                !downloadEvents.canActivateJob(it) -> {
//                    println("it is not cultivatable")
                    false
                }

                else -> true
            }
        }.also {
//            println("found downloadable queue item $it")
        }
    }


    private suspend fun persist() {
        val queue = getQueueModel()
        persistedData.setModel(queue)
    }

    //    suspend fun swapQueueItemToEnd(item: Long){
//        val currentIndex = queue.indexOf(item).takeIf { it > 0 } ?: return
//        queue.removeAt(currentIndex)
//        queue.add(item)
//        saveQueue()
//    }

    fun getOrder(item: Long): Int {
        return getQueueModel().queueItems.indexOf(item)
    }

    fun getQueueItemFromOrder(order: Int): Long {
        return getQueueModel().queueItems.toList()[order]
    }

    suspend fun addToQueue(item: Long) {
        _queueModel.update {
            it.copy(
                queueItems = it.queueItems
                    .plus(item)
                    .distinct()
            )
        }
        shake(
            itemChangeHappened = true
        )
    }

    fun clearQueue() {
        _queueModel.update {
            it.copy(queueItems = emptyList())
        }
        activeItems.clear()
        canceledItems.clear()
        trimmedItems.clear()
    }

    fun removeFromQueue(ids: List<Long>) {
        _queueModel.update {
            it.copy(
                queueItems = it.queueItems.filter {
                    it !in ids
                }.distinct()
            )
        }
        for (id in ids) {
            activeItems.remove(id)
            canceledItems.remove(id)
            trimmedItems.remove(id)
        }
    }

    fun removeFromQueue(id: Long) {
        removeFromQueue(listOf(id))
    }

    fun dispose() {
        cancelAutoStartJob()
        cancelAutoStopJob()
        listenerJob?.cancel()
        scope.cancel()
        listenerJob = null
    }
}

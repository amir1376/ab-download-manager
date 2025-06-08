package ir.amirab.downloader.queue

import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.downloader.db.IDownloadQueueDatabase
import ir.amirab.downloader.db.DownloadQueuePersistedDataAccess
import ir.amirab.downloader.db.QueueModel
import ir.amirab.util.ifThen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object DefaultQueueInfo {
    const val ID = 0L
    const val NAME = "Main"
}

class QueueManager(
    private val queueDb: IDownloadQueueDatabase,
    private val listOfJobs: DownloadManagerMinimalControl,
) {
    companion object {

        // we save this ids maybe later we want to add some queues
        const val RESERVED_UNTIL_QUEUE_ID = 10L
    }

    val queues = MutableStateFlow(
        emptyList<DownloadQueue>()
    )

    private suspend fun addDefaultQueue() {
        val queueModel = QueueModel(
            id = DefaultQueueInfo.ID,
            name = DefaultQueueInfo.NAME,
        )
//        println("creating default queue")
        queueDb.addQueue(queueModel)
        val queue = createQueue(queueModel)
        queues.update { currentList ->
            buildList {
                add(queue)
                addAll(currentList)
            }
        }
        queue.boot()
    }

    suspend fun addQueue(
        name: String,
    ) {
        val maxId = queueDb
            .getAllQueueIds()
            .maxOrNull()
            ?.coerceAtLeast(RESERVED_UNTIL_QUEUE_ID) ?: RESERVED_UNTIL_QUEUE_ID
        // this is reserved id
        val queueModel = QueueModel(
            id = maxId + 1,
            name = name,
        )
        queueDb.addQueue(
            queueModel
        )
        val queue = createQueue(queueModel)
        queues.update {
            it.plus(queue)
        }
        queue.boot()
    }

    suspend fun deleteQueue(
        queue: DownloadQueue
    ) {
        if (queue.isMainQueue()) {
            return
        }
        queue.dispose()
        queueDb.deleteQueue(queue.id)
        queues.update {
            it.filter {
                it.id != queue.id
            }
        }
    }

    suspend fun deleteQueue(
        id: Long,
    ) {
        val foundQueue = queues.value.find { it.id == id }
        foundQueue?.let {
            deleteQueue(foundQueue)
        }
    }

    private var booted = false
    private fun ensureBooted() {
        require(booted) {
            "please first boot QueueManager"
        }
    }

    suspend fun boot() {
        if (booted) {
            return
        }
        val queueModels = queueDb
            .getAllQueues()
        val dbQueues = queueModels.map {
            createQueue(it)
        }
        for (queue in dbQueues) {
            queue.boot()
        }
        queues.update { dbQueues }
        val ids = queueModels.map { it.id }
        if (DefaultQueueInfo.ID !in ids) {
            addDefaultQueue()
        }
        booted = true
    }

    fun getMainQueue(): DownloadQueue {
        ensureBooted()
        return requireNotNull(
            queues.value.find {
                it.id == DefaultQueueInfo.ID
            }
        ) { "we can't find main queue" }
    }

    private fun createQueue(queueModel: QueueModel): DownloadQueue {
        return DownloadQueue(
            persistedData = QueueInfoPersistedData(
                queueDb,
                queueModel.id
            ),
            downloadEvents = listOfJobs,
            persistedModel = queueModel,
        )
    }

    fun getAll(): List<DownloadQueue> {
        return queues.value
    }

    fun getQueue(queue: Long): DownloadQueue {
        return requireNotNull(
            queues.value.find {
                it.id == queue
            }
        )
    }

    fun canDelete(queue: Long): Boolean {
        return queue != DefaultQueueInfo.ID
        //        return if (queue in 0..RESERVED_UNTIL_QUEUE_ID) {
//            false
//        } else true
    }

    fun isItemInQueue(downloadId: Long): Boolean {
        return findItemInQueue(downloadId) != null
    }

    fun findItemInQueue(downloadId: Long): Long? {
        for (queue in queues.value) {
            for (queueItem in queue.getQueueModel().queueItems) {
                if (downloadId == queueItem) {
                    return queue.id
                }
            }
        }
        return null
    }

    suspend fun addToQueue(
        queueId: Long,
        downloadId: Long,
    ) {
        val foundInQueue = findItemInQueue(downloadId = downloadId)
        if (foundInQueue == queueId) {
            //already in same queue
            return
        }
        if (foundInQueue != null) {
            getQueue(foundInQueue).removeFromQueue(downloadId)
        }
        getQueue(queueId).addToQueue(downloadId)
    }

    suspend fun addToQueue(queueId: Long, downloadIds: List<Long>) {
        downloadIds.forEach {
            addToQueue(queueId, it)
        }
    }
}

private class QueueInfoPersistedData(
    val db: IDownloadQueueDatabase,
    val id: Long,
) : DownloadQueuePersistedDataAccess {
    var cached: QueueModel? = null
    val lock = Mutex()
    override suspend fun getModel(): QueueModel {
        if (cached == null) {
            cached = db.getQueue(id)
        }
//        println("getModel() == $cached")
        return cached!!
    }

    override suspend fun setModel(model: QueueModel) {
        if (model == cached) {
            //nothing to update
//            println("Noting to update")
            return
        }
        lock.withLock {
            db.updateQueue(model)
//            println("setModel() == $model")
            cached = model
        }
    }
}

private fun QueueManager.getActiveOrInactiveQueues(
    active: Boolean, scope: CoroutineScope,
): Flow<List<DownloadQueue>> {
    val output: MutableStateFlow<List<DownloadQueue>> = MutableStateFlow(emptyList())
    scope.launch {
        queues.collectLatest { latestQueues ->
            combine(
                latestQueues.map { it.activeFlow }
            ) {
                it.mapIndexedNotNull { index, value ->
                    val shouldAdd = if (active) value else !value
                    if (shouldAdd) {
                        latestQueues[index]
                    } else {
                        null
                    }
                }
            }.collect {
                output.value = it
            }
        }
    }
    return output
}

fun QueueManager.activeQueuesFlow(
    scope: CoroutineScope
): Flow<List<DownloadQueue>> {
    return getActiveOrInactiveQueues(true, scope)
}

fun QueueManager.inactiveQueuesFlow(
    scope: CoroutineScope
): Flow<List<DownloadQueue>> {
    return getActiveOrInactiveQueues(false, scope)
}

fun QueueManager.queueModelsFlow(
    scope: CoroutineScope
): StateFlow<List<QueueModel>> {
    val o = MutableStateFlow<List<QueueModel>>(emptyList())
    scope.launch {
        queues.collectLatest { queues ->
            coroutineScope {
                combine(
                    queues.map { queue -> queue.queueModel }
                ) { queueModelsArray ->
                    queueModelsArray.toList()
                }.collect {
                    o.value = it
                }
            }
        }
    }
    return o
}

fun DownloadQueue.isMainQueue(): Boolean {
    return DefaultQueueInfo.ID == id
}

package ir.amirab.downloader.db

import ir.amirab.downloader.queue.ScheduleTimes
import kotlinx.serialization.Serializable

/**
 * this is all states of queue that need to be persisted
 * */
@Serializable
data class QueueModel(
    val id:Long,
    val name: String,
    val maxConcurrent: Int = 2,
    val queueItems: List<Long> = emptyList(),
    val scheduledTimes: ScheduleTimes = ScheduleTimes.default(),
    val stopQueueOnEmpty:Boolean=false,
)
/**
 * CRUD all queues
 * */
interface IDownloadQueueDatabase {
    suspend fun getAllQueueIds(): List<Long>
    suspend fun getAllQueues(): List<QueueModel>
    suspend fun setAllQueues(queues: List<QueueModel>)
    suspend fun deleteAllQueues()
    suspend fun getQueue(queueId:Long):QueueModel
    suspend fun deleteQueue(queue: Long)
    suspend fun updateQueue(queue: QueueModel)
    suspend fun addQueue(queue: QueueModel)
}

/**
 * update a single queue (it is a view of a single queue in database)
 * this is passed to queue for access its persistent data and update it
 * setters must be implemented thread safe
 */
interface DownloadQueuePersistedDataAccess {

    suspend fun setModel(queue: QueueModel)
    suspend fun getModel():QueueModel

    suspend fun update(update: (QueueModel) -> QueueModel) {
        setModel(
            update(
                getModel()
            )
        )
    }
}
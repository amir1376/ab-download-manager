package ir.amirab.downloader.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private const val queueExtension = "json"

class DownloadQueueFileStorageDatabase(
    val fileSaver: TransactionalFileSaver,
    val queueFolder: File,
) : IDownloadQueueDatabase {
    private val lock = Mutex()
    private fun getFileOfQueue(queue: QueueModel): File {
        return getFileOfQueue(queue.id)
    }

    private fun getFileOfQueue(id: Long): File {
        return queueFolder.resolve("$id.json")
    }

    private fun getQueueFiles(): List<File> {
        return queueFolder.listFiles()
            .filter {
                it.isFile && it.extension == queueExtension
            }
    }


    override suspend fun getAllQueueIds(): List<Long> {
        return withContext(Dispatchers.IO) {
            getQueueFiles().map {
                it.name.substring(0,it.name.length-".$queueExtension".length)
            }.mapNotNull {
                it.toLongOrNull()
            }
        }
    }

    override suspend fun getQueue(queueId: Long): QueueModel {
        return withContext(Dispatchers.IO) {
            requireNotNull(fileSaver.readObject(
                getFileOfQueue(queueId)
            )
            ) {
                "Queue with $queueId returned null"
            }
        }
    }

    override suspend fun getAllQueues(): List<QueueModel> {
        return getAllQueueIds().mapNotNull {
            kotlin.runCatching {
                getQueue(it)
            }.getOrNull()
        }
    }

    override suspend fun deleteAllQueues() {
        getQueueFiles().forEach {
            it.delete()
        }
    }

    override suspend fun setAllQueues(queues: List<QueueModel>) {
        lock.withLock {
            deleteAllQueues()
            queues.forEach { addQueue(it) }
        }
    }

    override suspend fun deleteQueue(queueId: Long) {
        lock.withLock {
            getFileOfQueue(queueId).delete()
        }
    }

    override suspend fun updateQueue(queue: QueueModel) {
        lock.withLock {
            withContext(Dispatchers.IO) {
                fileSaver.writeObject(
                    getFileOfQueue(queue),
                    queue
                )
            }
        }
    }

    override suspend fun addQueue(queue: QueueModel) {
        val fileOfQueue = getFileOfQueue(queue)
        withContext(Dispatchers.IO) {
            fileSaver.writeObject(
                fileOfQueue,
                queue,
            )
        }
    }

}

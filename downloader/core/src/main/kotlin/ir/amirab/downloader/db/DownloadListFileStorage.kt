package ir.amirab.downloader.db

import ir.amirab.downloader.downloaditem.DownloadItem
import ir.amirab.downloader.utils.LockList
import ir.amirab.downloader.utils.SuspendLockList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class DownloadListFileStorage(
    private val downloadListFolder: File,
    private val fileSaver: TransactionalFileSaver,
) : IDownloadListDb {

    fun getDownloadItemFile(id: Long): File {
        return downloadListFolder.resolve("$id.json")
    }

    override suspend fun getAll(): List<DownloadItem> {
        return withContext(Dispatchers.IO) {
            downloadListFolder.listFiles()?.filter {
                it.name.endsWith(".json")
            }?.mapNotNull {
                get(it)
            }.orEmpty()
        }
    }

    private fun get(file: File): DownloadItem? {
        return fileSaver.readObject(file)
    }

    override suspend fun getById(id: Long): DownloadItem? {
        return withContext(Dispatchers.IO) {
            get(getDownloadItemFile(id))
        }
    }

    private val addLock = Mutex()
    override suspend fun add(item: DownloadItem) {
        withContext(Dispatchers.IO) {
            addLock.withLock {
                fileSaver.writeObject(getDownloadItemFile(item.id), item)
                val lastId = getLastId()
                if (lastId < item.id) {
                    setLastId(item.id)
                }
            }
        }
    }

    private val updateLocks = SuspendLockList(DownloadItem::id)
    override suspend fun update(item: DownloadItem) {
        withContext(Dispatchers.IO) {
            // we don't use same lock for all items , but create lock for each item
            updateLocks.withLock(item) {
                fileSaver.writeObject(getDownloadItemFile(item.id), item)
            }
        }
    }

    override suspend fun removeById(itemId: Long) {
        getDownloadItemFile(itemId).delete()
    }

    override suspend fun remove(item: DownloadItem) {
        removeById(item.id)
    }


    private val lastIdFile = downloadListFolder.resolve("last_id.txt")
    private fun setLastId(id: Long) {
        fileSaver.writeObject(lastIdFile, id)
    }

    override suspend fun getLastId(): Long {
        return withContext(Dispatchers.IO) {
            var lastId = fileSaver.readObject<Long>(lastIdFile)
            if (lastId == null) {
                lastId = getLastIdFromFiles()
                setLastId(lastId)
            }
            lastId
        }
    }

    private fun getLastIdFromFiles(): Long {
        return downloadListFolder.listFiles()!!.filter {
            it.name.endsWith(".json") && it.isFile
        }.map {
//                println(it.name)
            it.name.substring(0, it.name.length - ".json".length).also {
//                    println(it)
            }.toLong()
        }.maxOrNull() ?: -1L
    }
}
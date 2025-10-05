package ir.amirab.downloader.db

import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.utils.SuspendLockList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class DownloadListFileStorage(
    private val downloadListFolder: File,
    private val fileSaver: TransactionalFileSaver,
) : IDownloadListDb {

    private val fileLocks = SuspendLockList<Long>()

    fun getDownloadItemFile(id: Long): File {
        return downloadListFolder.resolve("$id.json")
    }

    override suspend fun getAll(): List<IDownloadItem> {
        return withContext(Dispatchers.IO) {
            val jsonExtension = ".json"
            downloadListFolder.listFiles()
                ?.mapNotNull { file ->
                    file.name
                        .takeIf { it.endsWith(jsonExtension) }
                        ?.removeSuffix(jsonExtension)
                        ?.toLongOrNull()
                        ?.let { get(file, it) }
                }.orEmpty()
        }
    }

    private suspend fun get(file: File, id: Long): IDownloadItem? {
        return fileLocks.withLock(id) {
            fileSaver.readObject(file)
        }
    }

    override suspend fun getById(id: Long): IDownloadItem? {
        return withContext(Dispatchers.IO) {
            get(getDownloadItemFile(id), id)
        }
    }

    private val addLock = Mutex()
    override suspend fun add(item: IDownloadItem) {
        withContext(Dispatchers.IO) {
            addLock.withLock {
                fileLocks.withLock(item.id) {
                    fileSaver.writeObject(getDownloadItemFile(item.id), item)
                    val lastId = getLastId()
                    if (lastId < item.id) {
                        setLastId(item.id)
                    }
                }
            }
        }
    }

    override suspend fun update(item: IDownloadItem) {
        withContext(Dispatchers.IO) {
            // we don't use same lock for all items , but create lock for each item
            fileLocks.withLock(item.id) {
                fileSaver.writeObject(getDownloadItemFile(item.id), item)
            }
        }
    }

    override suspend fun removeById(itemId: Long) {
        getDownloadItemFile(itemId).delete()
    }

    override suspend fun remove(item: IDownloadItem) {
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
        }.maxOfOrNull {
            it.name
                .substring(0, it.name.length - ".json".length)
                .toLong()
        } ?: -1L
    }
}

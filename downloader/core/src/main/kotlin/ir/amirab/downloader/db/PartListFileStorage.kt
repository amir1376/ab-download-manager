package ir.amirab.downloader.db

import ir.amirab.downloader.part.Parts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PartListFileStorage(
    val folder: File,
    val fileSaver: TransactionalFileSaver,
) : IDownloadPartListDb {
    fun getFileForId(id: Long): File {
        val resolve = folder.resolve("$id.json")
        return resolve
    }

    override suspend fun getParts(id: Long): Parts? {
        return withContext(Dispatchers.IO) {
            fileSaver.readObject(getFileForId(id))
        }
    }

    override suspend fun setParts(id: Long, parts: Parts) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val file = getFileForId(id)
                fileSaver.writeObject(file, parts)
            }
        }
    }

    override suspend fun removeParts(id: Long) {
        getFileForId(id).delete()
    }

    override suspend fun clear() {
        kotlin.runCatching {
            folder.listFiles()
        }.getOrNull()?.let {
            for (file in it) {
                file.delete()
            }
        }
    }
}

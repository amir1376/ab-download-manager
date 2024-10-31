package ir.amirab.downloader.db

import ir.amirab.downloader.downloaditem.DownloadItem
import java.io.File

interface IDownloadListDb {
    // modification/add implementations must be thread safe

    suspend fun getAll(): List<DownloadItem>
    suspend fun getById(id: Long): DownloadItem?
    suspend fun add(item: DownloadItem)
    suspend fun update(item: DownloadItem)
    suspend fun remove(item: DownloadItem)
    suspend fun removeById(itemId: Long)
    suspend fun getLastId(): Long

    suspend fun getFilePathById(id: Long): File?
}

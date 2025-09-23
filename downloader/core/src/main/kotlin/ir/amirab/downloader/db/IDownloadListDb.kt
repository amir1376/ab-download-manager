package ir.amirab.downloader.db

import ir.amirab.downloader.downloaditem.IDownloadItem


interface IDownloadListDb {
    // modification/add implementations must be thread safe

    suspend fun getAll(): List<IDownloadItem>
    suspend fun getById(id: Long): IDownloadItem?
    suspend fun add(item: IDownloadItem)
    suspend fun update(item: IDownloadItem)
    suspend fun remove(item: IDownloadItem)
    suspend fun removeById(itemId: Long)
    suspend fun getLastId(): Long

//    suspend fun allAsFlow(): Flow<List<DownloadItem>>
}


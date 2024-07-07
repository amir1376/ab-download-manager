package ir.amirab.downloader.db

import ir.amirab.downloader.part.Part

interface IDownloadPartListDb {
    suspend fun getParts(id: Long): List<Part>?
    suspend fun setParts(id: Long, parts: List<Part>)
    suspend fun clear()
    suspend fun removeParts(id: Long)
}
package ir.amirab.downloader.db

import ir.amirab.downloader.part.Part

class MemoryDownloadPartStatesDB : IDownloadPartListDb {
    private val list = mutableMapOf<Long, List<Part>>()
    override suspend fun getParts(id: Long): List<Part>? {
        return list[id]
    }

    override suspend fun setParts(id: Long, parts: List<Part>) {
        list[id] = parts.map { it.copy() }
    }

    override suspend fun removeParts(id: Long) {
        list.remove(id)
    }

    override suspend fun clear() {
        list.clear()
    }

}
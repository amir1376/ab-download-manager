package ir.amirab.downloader.db

import ir.amirab.downloader.part.Parts

class MemoryDownloadPartStatesDB : IDownloadPartListDb {
    private val list = mutableMapOf<Long, Parts>()
    override suspend fun getParts(id: Long): Parts? {
        return list[id]
    }

    override suspend fun setParts(id: Long, parts: Parts) {
        list[id] = parts.clone()
    }

    override suspend fun removeParts(id: Long) {
        list.remove(id)
    }

    override suspend fun clear() {
        list.clear()
    }

}

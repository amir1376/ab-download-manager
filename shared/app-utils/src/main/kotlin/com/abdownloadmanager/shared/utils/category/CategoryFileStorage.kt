package com.abdownloadmanager.shared.utils.category

import ir.amirab.downloader.db.TransactionalFileSaver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class CategoryFileStorage(
    val file: File,
    val fileSaver: TransactionalFileSaver,
) : CategoryStorage {
    val lock = Mutex()
    override suspend fun setCategories(categories: List<Category>) {
        lock.withLock {
            fileSaver.writeObject(file, categories)
        }
    }

    override suspend fun getCategories(): List<Category> {
        return fileSaver.readObject(file) ?: emptyList()
    }

    override suspend fun isCategoriesSet(): Boolean {
        return file.exists()
    }
}
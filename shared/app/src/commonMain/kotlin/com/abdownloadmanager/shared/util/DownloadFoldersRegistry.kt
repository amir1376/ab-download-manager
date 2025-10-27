package com.abdownloadmanager.shared.util

import okio.Path
import java.io.File

/**
 * this is used to boot when file permission are granted!
 */
class DownloadFoldersRegistry {
    private val foldersToCreate = mutableListOf<File>()
    fun boot() {
//        println("folder registery is $this")
        foldersToCreate.forEach {
            it.mkdirs()
        }
    }

    override fun toString(): String {
        return foldersToCreate.map {
            it.absolutePath
        }.joinToString("\n").let {
            "DownloadFoldersRegistry(\nlist=$it\n)"
        }
    }
    fun registerAndGet(folder: Path): File {
        val file = folder.toFile()
        foldersToCreate.add(file)
        return file
    }
}

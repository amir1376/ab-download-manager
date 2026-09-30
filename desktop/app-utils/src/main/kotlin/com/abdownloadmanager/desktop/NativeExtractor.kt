package com.abdownloadmanager.desktop

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.sink
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object NativeExtractor {
    private val cache = ConcurrentHashMap<String, File>()

    fun getFromResource(path: String): File {
        return cache.computeIfAbsent(path) { resourcePath ->
            val resource = resourcePath.toPath()
            val file = File.createTempFile("abdm_${resource.name}", null).apply {
                deleteOnExit()
            }

            try {
                FileSystem.RESOURCES.read(resource) {
                    file.sink().use {
                        readAll(it)
                    }
                }
                file.setExecutable(true, false)
                file
            } catch (t: Throwable) {
                file.delete()
                throw t
            }
        }
    }
}
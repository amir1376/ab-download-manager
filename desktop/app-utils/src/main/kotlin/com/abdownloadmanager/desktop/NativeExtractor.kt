package com.abdownloadmanager.desktop

import ir.amirab.util.nameWithoutExtension
import ir.amirab.util.readText
import ir.amirab.util.tryAtomicMove
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.sink
import java.io.File
import java.util.concurrent.ConcurrentHashMap


/**
 * each compiled binary should have a sha256 corresponding file
 * for example if we have a compiled setsid, we should also have a setsid.sig file as well
 */
object NativeExtractor {

    private lateinit var cacheDir: Path

    fun init(cacheDir: Path) {
        NativeExtractor.cacheDir = cacheDir
    }

    private val cache = ConcurrentHashMap<String, File>()

    fun getFromResource(path: String): File {
        return cache.computeIfAbsent(path) { resourcePath ->
            extractResource(resourcePath)
        }
    }

    private fun extractResource(resourcePath: String): File {
        val resource = resourcePath.toPath()
        val originalName = resource.name
        val nameWithoutExtension = resource.nameWithoutExtension

        val hash = getResourceHash(resourcePath)
        val targetDir = cacheDir.resolve(nameWithoutExtension).resolve(hash).toFile()
        // <cacheDir>/<nameWithoutExtension>/hash/<filename>
        val targetFile = targetDir.resolve(originalName)
        if (targetFile.isFile && targetFile.length() > 0) {
            targetFile.setExecutable(true, false)
            return targetFile
        }

        targetDir.mkdirs()

        val tempFile = File.createTempFile("tmp_${originalName}_", null, targetDir)

        try {
            FileSystem.RESOURCES.read(resource) {
                tempFile.sink().use { readAll(it) }
            }
            tempFile.tryAtomicMove(targetFile)
            targetFile.setExecutable(true, false)
            return targetFile
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        }
    }

    private fun getResourceHash(resourcePath: String): String {
        val hashResource = "$resourcePath.sig".toPath()
        return hashResource.readText(FileSystem.RESOURCES).trim()
    }
}

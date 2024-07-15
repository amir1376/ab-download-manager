package ir.amirab.downloader.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import java.io.File
import java.security.cert.Extension

object FileNameUtil {
    private fun getExtensionOrNull(name: String): String? {
        return name
            .substringAfterLast('.', "")
            .takeIf { it.isNotEmpty() }
    }

    fun numberedIfExists(file: File): Flow<File> {
        return flow {
            if (!file.exists()) {
                emit(file)
            }
            val ext = file.extension
                .takeIf { it.isNotEmpty() }
                ?.let { ".$it" }.orEmpty()
            val name = file.nameWithoutExtension
            var counter = 1
            while (currentCoroutineContext().isActive) {
                val newFile = file.parentFile.resolve(
                    "${name}_${counter}${ext}"
                )
                if (!newFile.exists()) {
                    emit(newFile)
                }
                counter++
            }
        }
    }

    fun replaceExtension(filename: String, newExtension: String, appendIfNotExists: Boolean=true): String {
        val ext = getExtensionOrNull(filename) ?: if (appendIfNotExists) {
            return "$filename.$newExtension"
        } else {
            return filename
        }
        val filenameWithoutExtension = filename.substring(0, filename.length - ext.length)
        return "$filenameWithoutExtension$newExtension"
    }
}
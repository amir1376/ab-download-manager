package ir.amirab.util

import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toOkioPath
import java.io.File


fun File.toUpUntil(
    condition: (File) -> Boolean
): File? {
    var file: File? = this
    while (true) {
        if (file == null) {
            return null
        }
        if (condition(file)) {
            return file
        }
        file = file.parentFile
    }
}

fun File.tryAtomicMove(destination: File) {
    val target = destination.toOkioPath()
    val source = toOkioPath()
    try {
        // this should replace existing target in java.nio file system
        // however if on some target we have to use java.io we should delete the file first
        FileSystem.SYSTEM.atomicMove(source, target)
    } catch (e: IOException) {
        if (!e.message.orEmpty().contains("atomic move")) {
            throw e
        }
        FileSystem.SYSTEM.delete(target, false)
        FileSystem.SYSTEM.copy(source, target)
        FileSystem.SYSTEM.delete(source)
    }
}

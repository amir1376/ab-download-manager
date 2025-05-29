package ir.amirab.util

import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException


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

fun File.atomicMove(destination: File) {
    val target = destination.toOkioPath()
    val source = toOkioPath()
    FileSystem.SYSTEM.atomicMove(source, target)
}

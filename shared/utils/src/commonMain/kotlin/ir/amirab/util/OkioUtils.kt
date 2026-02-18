package ir.amirab.util

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NotDirectoryException
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory

fun Path.writeText(
    text: String,
    fileSystem: FileSystem = FileSystem.SYSTEM
) {
    fileSystem.write(this, false) {
        writeUtf8(text)
    }
}

fun Path.readText(
    fileSystem: FileSystem = FileSystem.SYSTEM
): String {
    return fileSystem.read(this) {
        readUtf8()
    }
}

fun Path.exists(
    fileSystem: FileSystem = FileSystem.SYSTEM
): Boolean {
    return fileSystem.exists(this)
}

fun Path.isFile(
    fileSystem: FileSystem = FileSystem.SYSTEM
): Boolean {
    return fileSystem.metadataOrNull(this)?.isRegularFile == true
}

fun Path.isDirectory(
    fileSystem: FileSystem = FileSystem.SYSTEM
): Boolean {
    return fileSystem.metadataOrNull(this)?.isDirectory == true
}

fun Path.toAbsolute(
    fileSystem: FileSystem = FileSystem.SYSTEM
): Path {
    return ifThen(!isAbsolute) {
        fileSystem.canonicalize("".toPath()) / this
    }
}

fun Path.listFiles(fileSystem: FileSystem = FileSystem.SYSTEM): List<Path> {
    return fileSystem.list(this)
}
fun Path.listFilesOrNull(fileSystem: FileSystem = FileSystem.SYSTEM): List<Path>? {
    return fileSystem.listOrNull(this)
}

fun Path.pathString(): String = toString()

fun Path.createDirectories(
    fileSystem: FileSystem = FileSystem.SYSTEM
) {
    fileSystem.createDirectories(
        dir = this,
        mustCreate = false
    )
}

fun Path.createParentDirectories(
    fileSystem: FileSystem = FileSystem.SYSTEM
) {
    parent
        ?.takeIf { !it.isDirectory(fileSystem) }
        ?.createDirectories(fileSystem)
}

fun Path.deleteIfExists(
    fileSystem: FileSystem = FileSystem.SYSTEM
) {
    fileSystem.delete(this, false)
}

fun Path.startsWith(other: Path) = normalized().run {
    other.normalized().let { normalizedOther ->
        normalizedOther.segments.size <= segments.size &&
                segments
                    .slice(0 until normalizedOther.segments.size)
                    .filterIndexed { index, s -> normalizedOther.segments[index] != s }
                    .isEmpty()
    }
}

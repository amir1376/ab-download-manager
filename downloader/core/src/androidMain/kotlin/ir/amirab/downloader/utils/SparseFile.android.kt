package ir.amirab.downloader.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption

actual object SparseFile : ISparseFile {
    override fun createSparseFile(file: File): Boolean {
        if (!file.exists()) {
            val options = arrayOf<OpenOption>(
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.SPARSE
            )
            return runCatching {
                Files.newByteChannel(
                    file.toPath(),
                    *options,
                ).use {}
                true
            }.getOrElse { false }
        }
        return false
    }

    override fun canWeCreateSparseFile(file: File): Boolean {
        // android doesn't tell us!
        return true
    }
}

package ir.amirab.downloader.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileStore

interface ISparseFile {
    fun createSparseFile(file: File): Boolean
    fun canWeCreateSparseFile(file: File): Boolean
}

expect object SparseFile : ISparseFile

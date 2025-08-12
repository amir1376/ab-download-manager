package ir.amirab.downloader.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileStore


object SparseFile {
    private val fileSystemsSupportingSparseFiles = listOf(
        // Windows
        "NTFS", "ReFS",
        // Linux / Unix
        "ext4", "ext3", "ext2",
        "XFS", "Btrfs", "ZFS", "ReiserFS", "JFS", "F2FS",
        "UFS", "UFS2", "tmpfs", "OverlayFS",
        // macOS
        "APFS", "HFS+",
        // Network file systems (if server supports sparse files)
        "SMB", "CIFS", "NFS", "NFSv4",
    )
        .map { it.lowercase() }
        .toSet()

    fun createSparseFile(file: File): Boolean {
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

    /**
     * I assume that its parent are created before so make sure of that
     */
    fun canWeCreateSparseFile(file: File): Boolean {
        return kotlin.runCatching {
            val nearestFileExist = file.findNearestExistingFile() ?: return false
            val type = nearestFileExist
                .toPath()
                .fileStore()
                .type()
                .lowercase()
            // both must be lowercase
            fileSystemsSupportingSparseFiles.contains(type)
        }.getOrElse { false }
    }

    private fun File.findNearestExistingFile(): File? {
        var f: File? = this
        while (true) {
            if (f == null) {
                return null
            }
            if (f.exists()) {
                return f
            } else {
                f = f.parentFile
            }
        }

    }
}

package com.xeton.downloader.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileStore

actual object SparseFile : ISparseFile {
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

    override fun createSparseFile(file: File): Boolean {
        // [DELEGATED TO RUST]
        // Zero-copy sparse allocation is now handled natively via Rust core
        // (xeton_core/src/destination/mod.rs) using OS specific syscalls
        // (posix_fallocate, SetFileInformationByHandle, fcntl).
        return false
    }

    /**
     * I assume that its parent are created before so make sure of that
     */
    override fun canWeCreateSparseFile(file: File): Boolean {
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

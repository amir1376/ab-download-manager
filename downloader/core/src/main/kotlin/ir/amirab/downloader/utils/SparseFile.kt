package ir.amirab.downloader.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileStore


object SparseFile {
    private val fileSystemsSupportingSparseFiles = listOf(
        "NTFS",
        "ext4",
        "ext3",
        "XFS",
        "Btrfs",
        "ZFS",
        "ReiserFS",
        "APFS",
        "exFAT",
        "HFS+",
        "UFS",
        "ReFS"
    )

    fun createSparseFile(file: File): Boolean {
        if (!file.exists()) {
            val options = arrayOf<OpenOption>(
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.SPARSE
            )
            Files.newByteChannel(file.toPath(), *options).use {
                return true
            }
        }
        return false
    }

    /**
     * I assume that its parent are created before so make sure of that
     */
    fun canWeCreateSparseFile(file: File): Boolean {
        return kotlin.runCatching {
            val nearestFileExist = file.findNearestExistingFile()?:return false
            val type = nearestFileExist
                .toPath()
                .fileStore()
                .type()
            fileSystemsSupportingSparseFiles
                .find { it.equals(type, true) } != null
        }.getOrElse { false }
    }
    private fun File.findNearestExistingFile(): File? {
        var f:File? = this
        while (true){
            if (f==null){
                return null
            }
            if (f.exists()){
                return f
            }else{
                f = f.parentFile
            }
        }

    }
}

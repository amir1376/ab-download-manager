package ir.amirab.util.osfileutil

import java.io.File

interface FileUtils {
    fun openFile(file: File): Boolean
    fun openFileWith(file: File): Boolean
    fun openFolderOfFile(file: File): Boolean
    fun openFolder(folder: File): Boolean
    fun canWriteInThisFolder(folder: String): Boolean
    fun isRemovableStorage(path: String): Boolean

    open val isOpenFileSupported: Boolean get() = true
    open val isOpenFileWithSupported: Boolean get() = true
    open val isOpenFolderSupported: Boolean get() = true

    companion object : FileUtils by getPlatformFileUtil()
}

expect fun getPlatformFileUtil(): FileUtils

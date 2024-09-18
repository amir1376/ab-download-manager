package ir.amirab.util.osfileutil

import java.io.File
import java.io.FileNotFoundException

abstract class FileUtilsBase : FileUtils {
    override fun openFile(file: File): Boolean {
        if (!file.exists()) {
            throw FileNotFoundException("$file not found")
        }
        return openFileInternal(file)
    }

    override fun openFolderOfFile(file: File): Boolean {
        val file = file.canonicalFile.absoluteFile
        return openFolderOfFileInternal(file)
    }

    override fun canWriteInThisFolder(folder: String): Boolean {
        return runCatching {
            File(folder).canUseThisAsFolder()
        }.getOrElse { false }
    }

    private fun File.canUseThisAsFolder(): Boolean {
        var current: File? = this
        while (true) {
            if (current == null) break
            if (current.exists()) {
                return current.isDirectory
            }
            current = current.parentFile
        }
        return false
    }

    protected abstract fun openFileInternal(file: File): Boolean
    protected abstract fun openFolderOfFileInternal(file: File): Boolean
}
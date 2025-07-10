package ir.amirab.util.osfileutil

import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.fileStore

abstract class FileUtilsBase : FileUtils {
    override fun openFile(file: File): Boolean {
        return openFileInternal(
            file = preparedFile(file)
        )
    }

    override fun openFolderOfFile(file: File): Boolean {
        return openFolderOfFileInternal(
            file = preparedFile(file)
        )
    }

    override fun openFolder(folder: File): Boolean {
        return openFolderInternal(
            folder = preparedFile(folder)
        )
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

    private fun preparedFile(file: File): File {
        val file = file.canonicalFile.absoluteFile
        if (!file.exists()) {
            throw FileNotFoundException("$file not found")
        }
        return file
    }

    override fun isRemovableStorage(path: String): Boolean {
        runCatching {
            val store = Path(path).absolute().fileStore()
            if (store.supportsFileAttributeView("basic")) {
                val isRemovable = store.getAttribute("volume:isRemovable")
                if (isRemovable is Boolean) {
                    return isRemovable
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
        return false
    }

    protected abstract fun openFileInternal(file: File): Boolean
    protected abstract fun openFolderOfFileInternal(file: File): Boolean
    protected abstract fun openFolderInternal(folder: File): Boolean
}

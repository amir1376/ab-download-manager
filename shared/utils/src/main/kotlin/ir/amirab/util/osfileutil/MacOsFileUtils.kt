package ir.amirab.util.osfileutil

import java.io.File

internal class MacOsFileUtils : FileUtilsBase() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("open", file.path))
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("open", "-R", file.path))
    }
}
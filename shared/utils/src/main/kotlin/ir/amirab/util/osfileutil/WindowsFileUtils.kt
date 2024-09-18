package ir.amirab.util.osfileutil

import java.io.File

internal class WindowsFileUtils : FileUtilsBase() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("cmd", "/c", "start", "/B", "", file.path))
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("cmd", "/c", "explorer.exe", "/select,", file.path))
    }
}
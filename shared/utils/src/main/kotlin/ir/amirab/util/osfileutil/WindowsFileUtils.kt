package ir.amirab.util.osfileutil

import ir.amirab.util.execAndWait
import java.io.File

internal class WindowsFileUtils : FileUtilsBase() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("cmd", "/c", "start", "/B", "", file.path))
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("cmd", "/c", "explorer.exe", "/select,", file.path))
    }

    override fun openFolderInternal(folder: File): Boolean {
        return execAndWait(arrayOf("cmd", "/c", "explorer.exe", folder.path))
    }
}
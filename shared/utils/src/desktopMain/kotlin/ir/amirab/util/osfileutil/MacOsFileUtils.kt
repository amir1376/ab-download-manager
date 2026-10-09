package ir.amirab.util.osfileutil

import ir.amirab.util.execAndWait
import java.io.File

internal class MacOsFileUtils : DesktopFileUtils() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("open", file.path))
    }

    override fun openWithFileInternal(file: File): Boolean {
        TODO("openWith not yet implemented on macOS yet")
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("open", "-R", file.path))
    }

    override fun openFolderInternal(folder: File): Boolean {
        return execAndWait(arrayOf("open", folder.path))
    }

    // TODO remove it when its implemented
    override val isOpenFileWithSupported: Boolean = false
}

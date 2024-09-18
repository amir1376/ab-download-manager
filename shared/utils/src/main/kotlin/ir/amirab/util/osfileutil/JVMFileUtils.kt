package ir.amirab.util.osfileutil

import java.awt.Desktop
import java.io.File

/**
 * it uses the jvm default.
 */
internal class JVMFileUtils : FileUtilsBase() {
    override fun openFileInternal(file: File): Boolean {
        runCatching {
            Desktop.getDesktop().open(file)
            return true
        }
        return false
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        runCatching {
            Desktop.getDesktop().browseFileDirectory(file)
            return true
        }
        return false
    }
}
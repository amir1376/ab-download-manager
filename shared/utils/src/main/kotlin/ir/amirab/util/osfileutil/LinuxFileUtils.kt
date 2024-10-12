package ir.amirab.util.osfileutil

import java.io.File

internal class LinuxFileUtils : FileUtilsBase() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("xdg-open", file.path))
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        val dbusSendResult = execAndWait(
            arrayOf(
                "dbus-send",
                "--print-reply",
                "--dest=org.freedesktop.FileManager1",
                "/org/freedesktop/FileManager1",
                "org.freedesktop.FileManager1.ShowItems",
                "array:string:file://${file.path}",
                "string:"
            )
        )
        if (dbusSendResult) {
            return true
        }
        val xdgOpenResult = execAndWait(
            arrayOf("xdg-open", file.parent)
        )
        return xdgOpenResult
    }

    override fun openFolderInternal(folder: File): Boolean {
        return execAndWait(arrayOf("xdg-open", folder.parent))
    }
}
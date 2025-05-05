package ir.amirab.util.osfileutil

import ir.amirab.util.execAndWait
import java.io.File
import java.net.URLEncoder

internal class LinuxFileUtils : FileUtilsBase() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("xdg-open", file.path))
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        val uri = "file://" + encodePath(file.path)
        val dbusSendResult = execAndWait(
            arrayOf(
                "dbus-send",
                "--print-reply",
                "--dest=org.freedesktop.FileManager1",
                "/org/freedesktop/FileManager1",
                "org.freedesktop.FileManager1.ShowItems",
                "array:string:$uri",
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

    private fun encodePath(path: String): String {
        return path
            .split('/')
            .joinToString("/") {
                URLEncoder
                    .encode(it, Charsets.UTF_8)
                    .replace("+", "%20")
            }
    }
}

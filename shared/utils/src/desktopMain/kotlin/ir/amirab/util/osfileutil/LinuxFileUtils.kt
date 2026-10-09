package ir.amirab.util.osfileutil

import ir.amirab.util.execAndWait
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.FileDescriptor
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.types.Variant
import java.io.File
import java.net.URLEncoder

internal class LinuxFileUtils : DesktopFileUtils() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("xdg-open", file.path))
    }

    override fun openWithFileInternal(file: File): Boolean {
        return runCatching {
            file.inputStream().use { stream ->
                DBusConnectionBuilder.forSessionBus().build().use { connection ->
                    val portal = connection.getRemoteObject(
                        "org.freedesktop.portal.Desktop",
                        "/org/freedesktop/portal/desktop",
                        OpenURI::class.java,
                    )

                    portal.OpenFile(
                        "",
                        FileDescriptor.fromJavaFileDescriptor(
                            stream.fd,
                            null,
                        ),
                        mapOf("ask" to Variant(true)),
                    )
                }
            }
            true
        }.getOrElse {
            it.printStackTrace()
            false
        }
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


@DBusInterfaceName("org.freedesktop.portal.OpenURI")
interface OpenURI : DBusInterface {
    fun OpenFile(
        parentWindow: String,
        fd: FileDescriptor,
        options: Map<String, Variant<*>>,
    ): DBusPath
}

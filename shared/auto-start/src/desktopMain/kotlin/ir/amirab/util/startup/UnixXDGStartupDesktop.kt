package ir.amirab.util.startup

import java.io.File

class UnixXDGStartupDesktop(
    name: String,
    path: String,
    args: List<String>,
    val desktopEntryFileName: String,
) : AbstractDesktopStartupManager(
    name = name,
    path = path,
    args = args,
) {

    private fun getIconFilePath(): String? {
        return runCatching {
            val file = File(path)
            val name = file.name
            return file
                .parentFile.parentFile
                .resolve("lib/$name.png")
                .takeIf { it.exists() }?.path
        }.getOrNull()
    }

    private fun getAutoStartFile(): File {
        if (!autostartDir.exists()) {
            autostartDir.mkdirs()
        }
        return File(autostartDir, "$desktopEntryFileName.desktop")
    }

    @Throws(Exception::class)
    override fun install() {
        val name = this.name
        val exec = getExecutableWithArgs()
        val icon = getIconFilePath()
        getAutoStartFile().writeText(
            buildString {
                appendLine("[Desktop Entry]")
                appendLine("Type=Application")
                appendLine("Name=$name")
                appendLine("Exec=$exec")
                icon?.let { icon ->
                    appendLine("Icon=$icon")
                }
                appendLine("Terminal=false")
                appendLine("NoDisplay=true")
            }
        )
    }

    override fun uninstall() {
        getAutoStartFile().delete()
    }

    companion object {
        val autostartDir: File
            get() {
                val home = System.getProperty("user.home")

                return File("$home/.config/autostart/")
            }
    }
}

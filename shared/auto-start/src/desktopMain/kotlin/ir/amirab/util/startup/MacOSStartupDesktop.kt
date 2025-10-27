package ir.amirab.util.startup

import java.io.File

class MacOSStartupDesktop(
    name: String,
    path: String,
    args: List<String>,
) : AbstractDesktopStartupManager(
    path = path,
    name = name,
    args = args,
) {
    private fun getFile(): File {
        if (!launchAgentsDir.exists()) {
            launchAgentsDir.mkdirs()
        }

        return File(launchAgentsDir, super.name + ".plist")
    }

    @Throws(Exception::class)
    override fun install() {
        val file = getFile()

        val plistContent = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<plist version=\"1.0\">")
            appendLine("<dict>")
            appendLine("\t<key>Label</key>")
            appendLine("\t<string>${super.name}.startup</string>")
            appendLine("\t<key>ProgramArguments</key>")
            appendLine("\t<array>")
            appendLine("\t\t<string>/usr/bin/open</string>")
            appendLine("\t\t<string>-a</string>")
            appendLine("\t\t<string>${super.path}</string>")
            appendLine("\t\t<string>--args</string>")
            args.forEach {
                appendLine("\t\t<string>$it</string>")
            }
            appendLine("\t</array>")
            appendLine("\t<key>RunAtLoad</key>")
            appendLine("\t<true/>")
            appendLine("\t<key>LimitLoadToSessionType</key>")
            appendLine("\t<string>Aqua</string>")
            appendLine("</dict>")
            appendLine("</plist>")
        }

        file.bufferedWriter().use { it.write(plistContent) }

        runLaunchctlCommand(LOAD_COMMAND, file.path)
    }

    override fun uninstall() {
        val file = getFile()
        if (file.exists()) {
            runLaunchctlCommand(UNLOAD_COMMAND, file.path)
            file.delete()
        }
    }


    private fun runLaunchctlCommand(command: String, filePath: String) {
        ProcessBuilder("launchctl", command, filePath)
            .inheritIO()
            .start()
            .waitFor()
    }

    companion object {
        @get:Throws(Exception::class)
        val launchAgentsDir: File
            get() {
                var home = System.getProperty("user.home")

                if (Utils.isRoot) {
                    home = ""
                }

                return File("$home/Library/LaunchAgents/")
            }

        private const val UNLOAD_COMMAND = "unload"
        private const val LOAD_COMMAND = "load"
    }
}

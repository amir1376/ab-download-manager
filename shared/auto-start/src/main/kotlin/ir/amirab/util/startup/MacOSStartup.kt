package ir.amirab.util.startup

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class MacOSStartup(
    name: String,
    path: String,
    args: List<String>,
) : AbstractStartupManager(
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
        val out = PrintWriter(FileWriter(getFile()))
        out.println("<plist version=\"1.0\">")
        out.println("<dict>")
        out.println("\t<key>Label</key>")
        out.println("\t<string>" + super.name + "</string>")
        out.println("\t<key>ProgramArguments</key>")
        out.println("\t<array>")
        out.println("\t\t<string>" + getExecutableWithArgs() + "</string>")
        out.println("\t</array>")
        out.println("\t<key>RunAtLoad</key>")
        out.println("\t<true/>")
        out.println("</dict>")
        out.println("</plist>")
        out.close()
    }

    override fun uninstall() {
        getFile().delete()
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
    }
}

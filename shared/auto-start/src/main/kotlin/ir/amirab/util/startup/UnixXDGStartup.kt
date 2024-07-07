package ir.amirab.util.startup

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class UnixXDGStartup(
    name:String,
    path: String,
    isJar:Boolean=false
) : AbstractStartupManager(
    name = name,
    path = path,
    isJar = isJar
) {

    private fun getAutoStartFile(): File {
        if (!autostartDir.exists()) {
            autostartDir.mkdirs()
        }
        return File(autostartDir, super.name + ".desktop")
    }
    @Throws(Exception::class)
    override fun install() {
        val out = PrintWriter(FileWriter(getAutoStartFile()))
        out.println("[Desktop Entry]")
        out.println("Type=Application")
        out.println("Name=" + super.name)
        if (isJar) {
            out.println("Exec=java -jar '" + super.path + "'")
        } else {
            out.println("Exec=" + super.path)
        }
        out.println("Terminal=false")
        out.println("NoDisplay=true")
        out.close()

        val cmd = arrayOf("chmod", "+x", super.path)
        Runtime.getRuntime().exec(cmd)
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
package ir.amirab.util.startup

import ir.amirab.util.platform.Platform
import java.awt.GraphicsEnvironment
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object Utils {
    @get:Throws(Exception::class)
    val isRoot: Boolean
        get() = Platform.getCurrentPlatform() !== Platform.Desktop.Windows && BufferedReader(
            InputStreamReader(Runtime.getRuntime().exec("whoami").inputStream)
        ).readLine() == "root"

    val isHeadless: Boolean
        get() = GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()
}

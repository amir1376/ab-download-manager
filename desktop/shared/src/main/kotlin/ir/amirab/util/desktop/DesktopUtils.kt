package ir.amirab.util.desktop

import ir.amirab.util.desktop.linux.LinuxUtils
import ir.amirab.util.desktop.mac.MacOSUtils
import ir.amirab.util.desktop.windows.WindowsUtils
import ir.amirab.util.platform.Platform


interface DesktopUtils {
    fun openSystemProxySettings()

    companion object : DesktopUtils by getDesktopUtilOfCurrentOS()
}

private fun getDesktopUtilOfCurrentOS(): DesktopUtils {
    val platform = Platform.getCurrentPlatform() as Platform.Desktop
    return when (platform) {
        Platform.Desktop.Windows -> WindowsUtils()
        Platform.Desktop.MacOS -> MacOSUtils()
        Platform.Desktop.Linux -> LinuxUtils()
    }
}


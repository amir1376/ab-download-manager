package ir.amirab.util.desktop

import ir.amirab.util.desktop.utils.linux.LinuxUtils
import ir.amirab.util.desktop.utils.mac.MacOSUtils
import ir.amirab.util.desktop.utils.windows.WindowsUtils
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


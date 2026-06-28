package com.xeton.util.desktop

import com.xeton.util.desktop.poweraction.PowerAction
import com.xeton.util.desktop.utils.linux.LinuxUtils
import com.xeton.util.desktop.utils.mac.MacOSUtils
import com.xeton.util.desktop.utils.windows.WindowsUtils
import com.xeton.util.platform.Platform


interface DesktopUtils {
    fun openSystemProxySettings()
    fun powerAction(): PowerAction

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


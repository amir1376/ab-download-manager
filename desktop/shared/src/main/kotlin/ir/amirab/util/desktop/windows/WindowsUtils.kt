package ir.amirab.util.desktop.windows

import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.execAndWait

class WindowsUtils : DesktopUtils {
    override fun openSystemProxySettings() {
        val result = execAndWait(
            arrayOf(
                "cmd", "/c", "start",
                "ms-settings:network-proxy",
            )
        )
        if (!result) {
            execAndWait(
                arrayOf(
                    "rundll32.exe shell32.dll,Control_RunDLL inetcpl.cpl,,4"
                )
            )
        }
    }
}
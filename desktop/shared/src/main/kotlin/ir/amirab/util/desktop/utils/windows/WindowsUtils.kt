package ir.amirab.util.desktop.utils.windows

import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.desktop.keepawake.KeepAwake
import ir.amirab.util.desktop.keepawake.WindowsKeepAwake
import ir.amirab.util.desktop.poweraction.PowerAction
import ir.amirab.util.desktop.poweraction.PowerActionWindows
import ir.amirab.util.execAndWait

class WindowsUtils : DesktopUtils {
    private val keepAwakeService = WindowsKeepAwake()
    private val powerActionWindows = PowerActionWindows()
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

    override fun powerAction(): PowerAction {
        return powerActionWindows
    }

    override fun keepAwakeService(): KeepAwake {
        return keepAwakeService
    }
}

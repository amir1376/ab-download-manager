package com.xeton.util.desktop.utils.windows

import com.xeton.util.desktop.DesktopUtils
import com.xeton.util.desktop.poweraction.PowerAction
import com.xeton.util.desktop.poweraction.PowerActionWindows
import com.xeton.util.execAndWait

class WindowsUtils : DesktopUtils {
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
}

package com.xeton.util.desktop.utils.mac

import com.xeton.util.desktop.DesktopUtils
import com.xeton.util.desktop.poweraction.PowerAction
import com.xeton.util.desktop.poweraction.PowerActionMac
import com.xeton.util.execAndWait

class MacOSUtils : DesktopUtils {
    private val powerActionForMac = PowerActionMac()
    override fun openSystemProxySettings() {
        val commands = listOf(
            arrayOf("open", "x-apple.systempreferences:com.apple.Network-Settings.extension"),
            arrayOf("open", "/System/Library/PreferencePanes/Network.prefPane"),
            arrayOf("open", "/System/Preferences/Network")
        )

        for (command in commands) {
            if (execAndWait(command)) return
        }
    }

    override fun powerAction(): PowerAction {
        return powerActionForMac
    }
}

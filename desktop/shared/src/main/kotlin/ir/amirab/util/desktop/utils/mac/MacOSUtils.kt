package ir.amirab.util.desktop.utils.mac

import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.desktop.keepawake.KeepAwake
import ir.amirab.util.desktop.keepawake.MacKeepAwake
import ir.amirab.util.desktop.poweraction.PowerAction
import ir.amirab.util.desktop.poweraction.PowerActionMac
import ir.amirab.util.desktop.poweraction.PowerActionWindows
import ir.amirab.util.execAndWait

class MacOSUtils : DesktopUtils {
    private val keepAwakeService = MacKeepAwake()
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

    override fun keepAwakeService(): KeepAwake {
        return keepAwakeService
    }
}

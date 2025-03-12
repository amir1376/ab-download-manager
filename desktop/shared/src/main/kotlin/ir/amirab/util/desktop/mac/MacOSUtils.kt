package ir.amirab.util.desktop.mac

import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.execAndWait

class MacOSUtils : DesktopUtils {
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
}
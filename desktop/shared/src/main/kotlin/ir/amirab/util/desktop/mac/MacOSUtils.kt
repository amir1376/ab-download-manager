package ir.amirab.util.desktop.mac

import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.execAndWait

class MacOSUtils : DesktopUtils {
    override fun openSystemProxySettings() {
        execAndWait(
            arrayOf("open /System/Library/PreferencePanes/Network.prefPane")
        )
    }
}
package ir.amirab.util.desktop.utils.linux

import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.desktop.keepawake.KeepAwake
import ir.amirab.util.desktop.poweraction.PowerAction
import ir.amirab.util.desktop.poweraction.PowerActionLinux
import ir.amirab.util.execAndWait

class LinuxUtils : DesktopUtils {
    private val keepAwake = KeepAwake.NoOpKeepAwake()
    private val powerActionForLinux = PowerActionLinux()
    override fun openSystemProxySettings() {
        val desktopEnv = System.getenv("XDG_CURRENT_DESKTOP")
        when {
            desktopEnv?.contains("GNOME") ?: false -> {
                execAndWait(
                    arrayOf(
                        "gnome-control-center network"
                    )
                )
            }

            desktopEnv?.contains("KDE") ?: false -> {
                execAndWait(
                    arrayOf(
                        "systemsettings5 proxy"
                    )
                )
            }

            else -> {
                println("Can't open System Proxy Settings: Unsupported desktop environment: $desktopEnv")
            }
        }
    }

    override fun powerAction(): PowerAction {
        return powerActionForLinux
    }

    override fun keepAwakeService(): KeepAwake {
        return keepAwake
    }
}

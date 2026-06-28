package com.xeton.util.desktop.utils.linux

import com.xeton.util.desktop.DesktopUtils
import com.xeton.util.desktop.poweraction.PowerAction
import com.xeton.util.desktop.poweraction.PowerActionLinux
import com.xeton.util.execAndWait

class LinuxUtils : DesktopUtils {
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
}

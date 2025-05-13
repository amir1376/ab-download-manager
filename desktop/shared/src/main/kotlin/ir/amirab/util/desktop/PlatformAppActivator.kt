package ir.amirab.util.desktop

import ir.amirab.util.desktop.activator.mac.MacAppActivator
import ir.amirab.util.platform.Platform

interface PlatformAppActivator {
    fun active()

    companion object : PlatformAppActivator by getPlatformAppActivatorForCurrentOs()
}

object EmptyAppActivator : PlatformAppActivator {
    override fun active() {
        // Nothing
    }
}

private fun getPlatformAppActivatorForCurrentOs() = when (Platform.getCurrentPlatform()) {
    Platform.Desktop.MacOS -> MacAppActivator
    else -> EmptyAppActivator
}
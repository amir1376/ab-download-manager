package ir.amirab.util.desktop

import ir.amirab.util.desktop.activator.mac.MacAppActivator
import ir.amirab.util.platform.Platform

interface PlatformAppActivator {
    fun active()

    companion object : PlatformAppActivator by getPlatformAppActivatorForCurrentOs()
}

class EmptyAppActivator : PlatformAppActivator {
    override fun active() {
        // no-op
    }
}

private fun getPlatformAppActivatorForCurrentOs() = when (Platform.getCurrentPlatform()) {
    Platform.Desktop.MacOS -> MacAppActivator()
    else -> EmptyAppActivator()
}
package ir.amirab.util.desktop

import ir.amirab.util.desktop.dock.mac.MacDockToggler
import ir.amirab.util.platform.Platform

interface PlatformDockToggler {
    fun show()
    fun hide()

    companion object : PlatformDockToggler by getForCurrentOs()
}


object EmptyDockDockToggler : PlatformDockToggler {
    override fun show() {
        // no-op
    }

    override fun hide() {
        // no-op
    }
}

private fun getForCurrentOs() = when (Platform.getCurrentPlatform()) {
    Platform.Desktop.MacOS -> MacDockToggler()
    else -> EmptyDockDockToggler
}
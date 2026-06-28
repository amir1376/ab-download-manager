package com.xeton.util.desktop

import com.xeton.util.desktop.dock.mac.MacDockToggler
import com.xeton.util.platform.Platform

interface PlatformDockToggler {
    fun show()
    fun hide()

    companion object : PlatformDockToggler by getForCurrentOs()
}


class EmptyDockDockToggler : PlatformDockToggler {
    override fun show() {
        // no-op
    }

    override fun hide() {
        // no-op
    }
}

private fun getForCurrentOs() = when (Platform.getCurrentPlatform()) {
    Platform.Desktop.MacOS -> MacDockToggler()
    else -> EmptyDockDockToggler()
}
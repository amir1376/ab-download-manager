package ir.amirab.util.desktop.progress

import ir.amirab.util.desktop.progress.mac.MacFileProgress
import ir.amirab.util.platform.Platform

interface PlatformFileProgress {
    fun start(path: String): FileProgressHandle?

    companion object : PlatformFileProgress by getForCurrentOs()
}

interface FileProgressHandle {
    fun update(completed: Long, total: Long)
    fun end()
}

private class EmptyFileProgress : PlatformFileProgress {
    override fun start(path: String): FileProgressHandle? = null
}

private fun getForCurrentOs() = when (Platform.getCurrentPlatform()) {
    Platform.Desktop.MacOS -> MacFileProgress()
    else -> EmptyFileProgress()
}

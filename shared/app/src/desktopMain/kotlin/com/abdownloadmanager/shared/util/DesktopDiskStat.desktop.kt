package com.abdownloadmanager.shared.util

import ir.amirab.downloader.utils.IDiskStat
import java.io.File

actual typealias PlatformDiskStat = DesktopDiskStat

class DesktopDiskStat : IDiskStat {
    override fun getRemainingSpace(path: File): Long {
        return path.freeSpace
    }
}

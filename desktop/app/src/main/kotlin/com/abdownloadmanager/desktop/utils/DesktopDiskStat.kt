package com.abdownloadmanager.desktop.utils

import ir.amirab.downloader.utils.IDiskStat
import java.io.File

class DesktopDiskStat : IDiskStat {
    override fun getRemainingSpace(path: File): Long {
        return path.freeSpace
    }
}
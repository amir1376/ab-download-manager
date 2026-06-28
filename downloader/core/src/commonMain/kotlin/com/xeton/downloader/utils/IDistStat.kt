package com.xeton.downloader.utils

import java.io.File

interface IDiskStat {
    fun getRemainingSpace(path: File): Long
}

package com.abdownloadmanager.shared.util

import java.io.File

abstract class SystemDownloadLocationProvider {

    fun getDownloadLocation(): File {
        return runCatching { getCurrentDownloadLocation() }
            .onFailure { it.printStackTrace() }
            .getOrNull() ?: getCommonDownloadLocation()
    }

    /**
     * it should be a fixed path!
     * this meant to be used as fallback
     * - if the OS doesn't provide api to get download location dynamically
     * - or the [getCurrentDownloadLocation] fails for some reason
     * So, do your best to not throw exception here otherwise the [getDownloadLocation] will crash too!
     */
    protected abstract fun getCommonDownloadLocation(): File
    protected abstract fun getCurrentDownloadLocation(): File?
}

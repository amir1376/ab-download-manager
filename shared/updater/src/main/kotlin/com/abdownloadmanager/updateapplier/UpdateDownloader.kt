package com.abdownloadmanager.updateapplier

import com.abdownloadmanager.updatechecker.UpdateInfo
import java.io.File

interface UpdateDownloader {
    suspend fun downloadUpdate(updateInfo: UpdateInfo): File
    suspend fun removeUpdate(updateInfo: UpdateInfo)
}
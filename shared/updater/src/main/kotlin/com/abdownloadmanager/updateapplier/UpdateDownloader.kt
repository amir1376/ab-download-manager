package com.abdownloadmanager.updateapplier

import com.abdownloadmanager.updatechecker.UpdateSource
import java.io.File

interface UpdateDownloader {
    suspend fun downloadUpdate(updateDirectDownloadLink: UpdateSource.DirectDownloadLink): File
    suspend fun removeUpdate(updateDirectDownloadLink: UpdateSource.DirectDownloadLink)
    suspend fun removeAllUpdates()
}
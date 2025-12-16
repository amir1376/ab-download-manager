package com.abdownloadmanager

import java.io.File

fun interface UpdateDownloadLocationProvider {
    fun getSaveLocation(): File
}
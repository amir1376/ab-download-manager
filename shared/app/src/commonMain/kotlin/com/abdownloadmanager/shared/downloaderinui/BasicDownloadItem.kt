package com.abdownloadmanager.shared.downloaderinui

data class BasicDownloadItem(
    var folder: String,
    var name: String,
    var contentLength: Long = -1,
    var preferredConnectionCount: Int? = null,
    var speedLimit: Long = 0,
    var fileChecksum: String? = null
)

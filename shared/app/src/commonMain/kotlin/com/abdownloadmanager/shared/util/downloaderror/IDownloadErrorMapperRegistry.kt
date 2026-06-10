package com.abdownloadmanager.shared.util.downloaderror

interface IDownloadErrorMapperRegistry {
    fun getReason(throwable: Throwable): DownloadErrorReason?
}

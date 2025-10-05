package com.abdownloadmanager.shared.downloaderinui.http

import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem

fun PerHostSettingsItem.applyToHttpDownload(
    downloadCredentials: HttpDownloadCredentials
): HttpDownloadCredentials {
    var out = downloadCredentials
    applyToHttpDownload(
        setUsername = {
            out = out.copy(username = it)
        },
        setPassword = {
            out = out.copy(password = it)
        },
        setUserAgent = {
            out = out.copy(userAgent = it)
        },
        setThreadCount = {},
        setSpeedLimit = {}
    )
    return out
}

fun PerHostSettingsItem.applyToHttpDownload(
    downloadCredentials: HttpDownloadItem
): HttpDownloadItem {
    var out = downloadCredentials
    applyToHttpDownload(
        setUsername = {
            out = out.copy(username = it)
        },
        setPassword = {
            out = out.copy(password = it)
        },
        setUserAgent = {
            out = out.copy(userAgent = it)
        },
        setThreadCount = {
            out = out.copy(preferredConnectionCount = it)
        },
        setSpeedLimit = {
            out = out.copy(speedLimit = it)
        }
    )
    return out
}

fun PerHostSettingsItem.applyToHttpDownload(
    setUsername: (String) -> Unit,
    setPassword: (String) -> Unit,
    setUserAgent: (String) -> Unit,
    setThreadCount: (Int) -> Unit,
    setSpeedLimit: (Long) -> Unit,
) {
    username?.let(setUsername)
    password?.let(setPassword)
    userAgent?.let(setUserAgent)
    threadCount?.let(setThreadCount)
    speedLimit?.let(setSpeedLimit)
}

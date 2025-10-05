package com.abdownloadmanager.shared.downloaderinui.http.add

import com.abdownloadmanager.shared.utils.DownloadSystem
import com.abdownloadmanager.shared.downloaderinui.DownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.LinkCheckerFactory
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import kotlinx.coroutines.CoroutineScope

class HttpDownloadUiChecker(
    initialCredentials: HttpDownloadCredentials = HttpDownloadCredentials.Companion.empty(),
    linkCheckerFactory: LinkCheckerFactory<HttpDownloadCredentials, HttpResponseInfo, HttpLinkChecker>,
    initialFolder: String,
    initialName: String = "",
    downloadSystem: DownloadSystem,
    scope: CoroutineScope,
) : DownloadUiChecker<HttpDownloadCredentials, HttpResponseInfo, HttpLinkChecker>(
    initialCredentials, linkCheckerFactory, initialFolder, initialName, downloadSystem, scope
) {
    val length = linkChecker.length
}

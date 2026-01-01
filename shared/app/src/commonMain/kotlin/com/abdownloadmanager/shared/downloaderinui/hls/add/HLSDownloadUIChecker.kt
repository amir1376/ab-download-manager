package com.abdownloadmanager.shared.downloaderinui.hls.add

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.DownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.LinkCheckerFactory
import ir.amirab.downloader.downloaditem.hls.HLSDownloadCredentials
import com.abdownloadmanager.shared.downloaderinui.hls.HLSLinkChecker
import ir.amirab.downloader.downloaditem.hls.HLSResponseInfo
import com.abdownloadmanager.shared.util.DownloadSystem
import kotlinx.coroutines.CoroutineScope

class HLSDownloadUIChecker(
    initCredentials: HLSDownloadCredentials,
    linkCheckerFactory: LinkCheckerFactory<HLSDownloadCredentials, HLSResponseInfo, DownloadSize.Duration, HLSLinkChecker>,
    initialFolder: String,
    initialName: String,
    downloadSystem: DownloadSystem,
    scope: CoroutineScope,
) : DownloadUiChecker<HLSDownloadCredentials, HLSResponseInfo, DownloadSize.Duration, HLSLinkChecker>(
    initialCredentials = initCredentials,
    linkCheckerFactory = linkCheckerFactory,
    initialFolder = initialFolder,
    initialName = initialName,
    downloadSystem = downloadSystem,
    scope = scope,
) {
}

package com.abdownloadmanager.shared.downloaderinui.hls.add

import com.abdownloadmanager.shared.downloaderinui.DownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.LinkCheckerFactory
import com.abdownloadmanager.shared.downloaderinui.hls.HLSDownloadCredentials
import com.abdownloadmanager.shared.downloaderinui.hls.HLSLinkChecker
import ir.amirab.downloader.downloaditem.hls.HLSResponseInfo
import com.abdownloadmanager.shared.utils.DownloadSystem
import kotlinx.coroutines.CoroutineScope

class HLSDownloadUIChecker(
    initCredentials: HLSDownloadCredentials,
    linkCheckerFactory: LinkCheckerFactory<HLSDownloadCredentials, HLSResponseInfo, HLSLinkChecker>,
    initialFolder: String,
    initialName: String,
    downloadSystem: DownloadSystem,
    scope: CoroutineScope,
) : DownloadUiChecker<HLSDownloadCredentials, HLSResponseInfo, HLSLinkChecker>(
    initialCredentials = initCredentials,
    linkCheckerFactory = linkCheckerFactory,
    initialFolder = initialFolder,
    initialName = initialName,
    downloadSystem = downloadSystem,
    scope = scope,
) {
    val duration = linkChecker.duration
}

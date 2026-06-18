package com.abdownloadmanager.shared.downloaderinui.ytdlp.add

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.util.DownloadSystem
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.LinkCheckerFactory
import com.abdownloadmanager.shared.downloaderinui.ytdlp.YtdlpLinkChecker
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadCredentials
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpResponseInfo
import kotlinx.coroutines.CoroutineScope

class YtdlpNewDownloadUiChecker(
    initialCredentials: YtdlpDownloadCredentials,
    linkCheckerFactory: LinkCheckerFactory<YtdlpDownloadCredentials, YtdlpResponseInfo, DownloadSize.Bytes, YtdlpLinkChecker>,
    initialFolder: String,
    initialName: String = "",
    downloadSystem: DownloadSystem,
    scope: CoroutineScope,
) : NewDownloadUiChecker<YtdlpDownloadCredentials, YtdlpResponseInfo, DownloadSize.Bytes, YtdlpLinkChecker>(
    initialCredentials, linkCheckerFactory, initialFolder, initialName, downloadSystem, scope
)

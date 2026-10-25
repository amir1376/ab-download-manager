package com.abdownloadmanager.shared.downloaderinui.torrent.add

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadUiChecker
import com.abdownloadmanager.shared.downloaderinui.LinkCheckerFactory
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadCredentials
import com.abdownloadmanager.shared.downloaderinui.torrent.TorrentLinkChecker
import ir.amirab.downloader.downloaditem.torrent.TorrentResponseInfo
import com.abdownloadmanager.shared.util.DownloadSystem
import kotlinx.coroutines.CoroutineScope

class TorrentNewDownloadUIChecker(
    initCredentials: TorrentDownloadCredentials,
    linkCheckerFactory: LinkCheckerFactory<TorrentDownloadCredentials, TorrentResponseInfo, DownloadSize.Duration, TorrentLinkChecker>,
    initialFolder: String,
    initialName: String,
    downloadSystem: DownloadSystem,
    scope: CoroutineScope,
) : NewDownloadUiChecker<TorrentDownloadCredentials, TorrentResponseInfo, DownloadSize.Duration, TorrentLinkChecker>(
    initialCredentials = initCredentials,
    linkCheckerFactory = linkCheckerFactory,
    initialFolder = initialFolder,
    initialName = initialName,
    downloadSystem = downloadSystem,
    scope = scope,
) {
}

package com.abdownloadmanager.shared.downloaderinui.torrent

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.util.FilenameFixer
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadCredentials
import ir.amirab.downloader.downloaditem.torrent.TorrentLinks
import ir.amirab.downloader.downloaditem.torrent.TorrentResponseInfo
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TorrentLinkChecker(
    credentials: TorrentDownloadCredentials,
    @Suppress("unused") private val client: HttpDownloaderClient,
) : LinkChecker<TorrentDownloadCredentials, TorrentResponseInfo, DownloadSize.Duration>(
    initialCredentials = credentials
) {
    private val _suggestedName = MutableStateFlow<String?>(null)
    override val suggestedName: StateFlow<String?> = _suggestedName.asStateFlow()
    private val _duration = MutableStateFlow<Double?>(null)
    val duration: StateFlow<Double?> = _duration.asStateFlow()
    override val downloadSize: StateFlow<DownloadSize.Duration?> = _duration.mapStateFlow {
        it?.let(DownloadSize::Duration)
    }

    override fun isLinkSupported(link: String): Boolean = TorrentLinks.isSupported(link)

    override fun infoUpdated(responseInfo: TorrentResponseInfo?) {
        _suggestedName.value = responseInfo?.name?.let(FilenameFixer::fix)
        _duration.value = responseInfo?.duration
    }

    // stub: no network yet, real metadata fetching comes with libtorrent
    override suspend fun actualCheck(credentials: TorrentDownloadCredentials): TorrentResponseInfo {
        return TorrentResponseInfo(name = TorrentLinks.guessName(credentials.link))
    }
}

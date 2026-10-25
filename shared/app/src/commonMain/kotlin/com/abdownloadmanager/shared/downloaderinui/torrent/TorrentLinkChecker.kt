package com.abdownloadmanager.shared.downloaderinui.torrent

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.util.FilenameFixer
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadCredentials
import ir.amirab.downloader.downloaditem.torrent.TorrentResponseInfo
import ir.amirab.util.HttpUrlUtils
import ir.amirab.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TorrentLinkChecker(
    credentials: TorrentDownloadCredentials,
    private val client: HttpDownloaderClient,
) : LinkChecker<TorrentDownloadCredentials, TorrentResponseInfo, DownloadSize.Duration>(
    initialCredentials = credentials
) {
    private val _suggestedName: MutableStateFlow<String?> = MutableStateFlow(null)
    override val suggestedName: StateFlow<String?> = MutableStateFlow(null)
    private val _duration: MutableStateFlow<Double?> = MutableStateFlow(null)
    val duration: StateFlow<Double?> = _duration.asStateFlow()
    override val downloadSize: StateFlow<DownloadSize.Duration?> = _duration.mapStateFlow {
        it?.let(DownloadSize::Duration)
    }
    override fun infoUpdated(responseInfo: TorrentResponseInfo?) {
        _suggestedName.value = responseInfo?.name ?: HttpUrlUtils.extractNameFromLink(credentials.value.link)
            ?.let(FilenameFixer::fix)
        _duration.value = responseInfo?.duration
    }

    override suspend fun actualCheck(credentials: TorrentDownloadCredentials): TorrentResponseInfo {
        return client.connect(credentials, null, null)
            .use { TorrentResponseInfo.fromConnection(it) }
    }
}

package ir.amirab.downloader.downloaditem.torrent

import io.lindstrom.m3u8.model.MediaPlaylist
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig

data class TorrentDownloadJobExtraConfig(
    val hlsManifest: MediaPlaylist? = null
) : DownloadJobExtraConfig

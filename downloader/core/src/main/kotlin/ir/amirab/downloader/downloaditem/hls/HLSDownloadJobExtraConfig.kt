package ir.amirab.downloader.downloaditem.hls

import io.lindstrom.m3u8.model.MediaPlaylist
import ir.amirab.downloader.downloaditem.DownloadJobExtraConfig

data class HLSDownloadJobExtraConfig(
    val hlsManifest: MediaPlaylist? = null
) : DownloadJobExtraConfig

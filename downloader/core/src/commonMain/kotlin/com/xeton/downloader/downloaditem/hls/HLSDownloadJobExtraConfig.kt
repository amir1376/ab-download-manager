package com.xeton.downloader.downloaditem.hls

import io.lindstrom.m3u8.model.MediaPlaylist
import com.xeton.downloader.downloaditem.DownloadJobExtraConfig

data class HLSDownloadJobExtraConfig(
    val hlsManifest: MediaPlaylist? = null
) : DownloadJobExtraConfig

package com.abdownloadmanager.shared.downloaderinui.hls

import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.downloaditem.hls.HLSResponseInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HLSLinkChecker(
    credentials: HLSDownloadCredentials,
    private val client: HttpDownloaderClient,
) : LinkChecker<HLSDownloadCredentials, HLSResponseInfo>(
    initialCredentials = credentials
) {
    private val _suggestedName: MutableStateFlow<String?> = MutableStateFlow(null)
    override val suggestedName: StateFlow<String?> = MutableStateFlow(null)
    private val _duration: MutableStateFlow<Double?> = MutableStateFlow(null)
    val duration: StateFlow<Double?> = _duration.asStateFlow()
    override fun infoUpdated(responseInfo: HLSResponseInfo?) {
        _suggestedName.value = responseInfo?.name
        _duration.value = responseInfo?.duration
    }

    override suspend fun actualCheck(credentials: HLSDownloadCredentials): HLSResponseInfo {
        return client.connect(credentials, null, null)
            .use { HLSResponseInfo.fromConnection(it) }
    }
}

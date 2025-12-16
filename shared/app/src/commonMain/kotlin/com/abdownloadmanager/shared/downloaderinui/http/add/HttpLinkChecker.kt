package com.abdownloadmanager.shared.downloaderinui.http.add

import com.abdownloadmanager.shared.util.FilenameFixer
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HttpLinkChecker(
    initialCredentials: HttpDownloadCredentials = HttpDownloadCredentials.Companion.empty(),
    private val client: HttpDownloaderClient,
) : LinkChecker<HttpDownloadCredentials, HttpResponseInfo>(initialCredentials) {
    private val _suggestedName = MutableStateFlow(null as String?)
    override val suggestedName = _suggestedName.asStateFlow()

    private val _length = MutableStateFlow(null as Long?)
    val length = _length.asStateFlow()

    override fun infoUpdated(responseInfo: HttpResponseInfo?) {
        updateNameAndLength(responseInfo)
    }

    override suspend fun actualCheck(credentials: HttpDownloadCredentials): HttpResponseInfo {
        return client.test(credentials)
    }

    private fun updateNameAndLength(responseInfo: HttpResponseInfo?) {
        val suggestedName = responseInfo
            ?.fileName
            ?.let(FilenameFixer::fix)
        val length = responseInfo?.run {
            totalLength.takeIf { isSuccessFul }
        }
        _suggestedName.update { suggestedName }
        _length.update { length }
    }
}

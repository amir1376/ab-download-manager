package com.abdownloadmanager.shared.downloaderinui.http.add

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.util.FilenameFixer
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.xeton.downloader.connection.HttpDownloaderClient
import com.xeton.downloader.connection.response.HttpResponseInfo
import com.xeton.downloader.downloaditem.http.HttpDownloadCredentials
import com.xeton.util.HttpUrlUtils
import com.xeton.util.flow.mapStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HttpLinkChecker(
    initialCredentials: HttpDownloadCredentials = HttpDownloadCredentials.empty(),
    private val client: HttpDownloaderClient,
) : LinkChecker<HttpDownloadCredentials, HttpResponseInfo, DownloadSize.Bytes>(initialCredentials) {
    private val _suggestedName = MutableStateFlow(null as String?)
    override val suggestedName = _suggestedName.asStateFlow()

    private val _length = MutableStateFlow(null as Long?)
    override val downloadSize = _length.mapStateFlow {
        it?.let(DownloadSize::Bytes)
    }

    override fun infoUpdated(responseInfo: HttpResponseInfo?) {
        updateNameAndLength(responseInfo)
    }

    override suspend fun actualCheck(credentials: HttpDownloadCredentials): HttpResponseInfo {
        return client.test(credentials)
    }

    private fun updateNameAndLength(responseInfo: HttpResponseInfo?) {
        val suggestedName = responseInfo
            ?.fileName ?: HttpUrlUtils.extractNameFromLink(credentials.value.link)
            ?.let(FilenameFixer::fix)
        val length = responseInfo?.run {
            totalLength.takeIf { isSuccessFul }
        }
        _suggestedName.update { suggestedName }
        _length.update { length }
    }
}

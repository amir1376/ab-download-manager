package com.abdownloadmanager.shared.utils

import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.connection.response.ResponseInfo
import ir.amirab.downloader.downloaditem.DownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.UrlUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LinkChecker(
    initialCredentials: IDownloadCredentials = DownloadCredentials.empty(),
    private val client: DownloaderClient,
) {
    //input
    val credentials = MutableStateFlow(initialCredentials)

    private val _suggestedName = MutableStateFlow(null as String?)
    val suggestedName = _suggestedName.asStateFlow()

    private val _length = MutableStateFlow(null as Long?)
    val length = _length.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _responseInfo = MutableStateFlow(null as ResponseInfo?)
    val responseInfo = _responseInfo.asStateFlow()

    private val _isValid = MutableStateFlow(false)
    private val isValid = _isValid.asStateFlow()

    private fun setInfo(responseInfo: ResponseInfo?) {
        _responseInfo.update { responseInfo }
        updateNameAndLength(responseInfo)
        validate()
    }

    private fun validate() {
        val isValid = when {
            !UrlUtils.isValidUrl(this.credentials.value.link) -> false
            else -> true
        }
        _isValid.update { isValid }
    }

    suspend fun check() {
        val downloadCredentials = credentials.value
        val link = downloadCredentials.link
        val isValidUrl = UrlUtils.isValidUrl(link)
        setInfo(null)
        if (link.isBlank() || !isValidUrl) {
            return
        }
        _isLoading.update { true }
        val info = runCatching {
            client.test(downloadCredentials)
        }.getOrNull()
        _isLoading.update { false }
        setInfo(info)
    }


    private fun updateNameAndLength(responseInfo: ResponseInfo?) {
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

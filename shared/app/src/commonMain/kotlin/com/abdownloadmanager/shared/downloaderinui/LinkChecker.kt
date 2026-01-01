package com.abdownloadmanager.shared.downloaderinui

import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.HttpUrlUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class LinkChecker<
        Credentials : IDownloadCredentials,
        ResponseInfo : IResponseInfo,
        TDownloadSize : DownloadSize,
        >(
    initialCredentials: Credentials
) {
    //input
    val credentials = MutableStateFlow(initialCredentials)

    abstract val suggestedName: StateFlow<String?>
    abstract val downloadSize: StateFlow<TDownloadSize?>

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _responseInfo = MutableStateFlow<ResponseInfo?>(null)
    val responseInfo = _responseInfo.asStateFlow()

    private val _isValid = MutableStateFlow(false)
    private val isValid = _isValid.asStateFlow()

    abstract fun infoUpdated(responseInfo: ResponseInfo?)
    abstract suspend fun actualCheck(credentials: Credentials): ResponseInfo
    fun isValidCredentials(credentials: Credentials): Boolean {
        return runCatching {
            credentials.validateCredentials()
            true
        }.getOrElse { false }
    }

    private fun setInfo(responseInfo: ResponseInfo?) {
        _responseInfo.update { responseInfo }
        infoUpdated(responseInfo)
        validate()
    }

    private fun validate() {
        val isValid = isValidCredentials(this.credentials.value)
        _isValid.update { isValid }
    }

    suspend fun check() {
        val downloadCredentials = credentials.value
        val link = downloadCredentials.link
        val isValidUrl = HttpUrlUtils.isValidUrl(link)
        setInfo(null)
        if (link.isBlank() || !isValidUrl) {
            return
        }
        _isLoading.update { true }
        val info = runCatching {
            actualCheck(downloadCredentials)
        }.getOrNull()
        _isLoading.update { false }
        setInfo(info)
    }
}

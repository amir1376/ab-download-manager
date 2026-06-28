package com.xeton.downloader.downloaditem.hls

import arrow.core.Option
import arrow.core.getOrElse
import com.xeton.downloader.downloaditem.IDownloadCredentials
import com.xeton.util.HttpUrlUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("hls")
data class HLSDownloadCredentials(
    override val headers: Map<String, String>? = null,
    override val username: String? = null,
    override val password: String? = null,
    override val userAgent: String? = null,
    override val link: String,
    override val downloadPage: String? = null
) : IHLSCredentials {
    override fun copy(
        link: Option<String>,
        downloadPage: Option<String?>
    ): IDownloadCredentials {
        return copy(
            link = link.getOrElse { this.link },
            downloadPage = downloadPage.getOrElse { this.downloadPage },
        )
    }

    override fun validateCredentials() {
        HttpUrlUtils.isValidUrl(link)
    }
}

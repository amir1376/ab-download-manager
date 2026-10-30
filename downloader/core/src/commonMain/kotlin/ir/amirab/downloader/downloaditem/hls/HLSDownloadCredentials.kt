package ir.amirab.downloader.downloaditem.hls

import arrow.core.Option
import arrow.core.getOrElse
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.IHttpDownloadCredentials
import ir.amirab.util.HttpUrlUtils
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

    companion object {
        fun from(credentials: IHLSCredentials): HLSDownloadCredentials {
            credentials.run {
                return when (this) {
                    is HLSDownloadCredentials -> this
                    else -> HLSDownloadCredentials(
                        link = link,
                        headers = headers,
                        username = username,
                        password = password,
                        downloadPage = downloadPage,
                        userAgent = userAgent,
                    )
                }
            }
        }
    }
}

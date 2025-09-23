package ir.amirab.downloader.downloaditem.http

import arrow.core.Option
import arrow.core.getOrElse
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.util.HttpUrlUtils
import kotlinx.serialization.Serializable

@Serializable
data class HttpDownloadCredentials(
    override val link: String,
    override val headers: Map<String, String>? = null,
    override val username: String? = null,
    override val password: String? = null,
    override val downloadPage: String? = null,
    override val userAgent: String? = null,
) : IHttpDownloadCredentials {
    override fun validateCredentials() {
        validate(this)
    }

    override fun copy(
        link: Option<String>,
        downloadPage: Option<String?>
    ): IDownloadCredentials {
        return copy(
            link = link.getOrElse { this.link },
            downloadPage = downloadPage.getOrElse { this.downloadPage }
        )
    }

    companion object {
        fun empty() = HttpDownloadCredentials(
            link = ""
        )

        fun from(credentials: IHttpDownloadCredentials): HttpDownloadCredentials {
            credentials.run {
                return when (this) {
                    is HttpDownloadCredentials -> this
                    else -> HttpDownloadCredentials(
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

        fun validate(credentials: IHttpDownloadCredentials) {
            //make sure url is valid
            require(HttpUrlUtils.isValidUrl(credentials.link)) {
                "url is not valid"
            }
        }
    }
}

interface IHttpDownloadCredentials : IDownloadCredentials {
    val headers: Map<String, String>?
    val username: String?
    val password: String?
    val userAgent: String?
}

package ir.amirab.downloader.downloaditem

import kotlinx.serialization.Serializable

@Serializable
data class DownloadCredentials(
    override val link: String,
    override val headers: Map<String, String>? = null,
    override val username: String? = null,
    override val password: String? = null,
    override val downloadPage: String? = null,
    override val userAgent: String? = null,
) : IDownloadCredentials{
    companion object {
        fun empty()=DownloadCredentials(
            link = ""
        )
        fun from(credentials: IDownloadCredentials): DownloadCredentials {
            credentials.run {
                return when (this) {
                    is DownloadCredentials -> this
                    else -> DownloadCredentials(
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

interface IDownloadCredentials {
    val link: String
    val headers: Map<String, String>?
    val username: String?
    val password: String?
    val downloadPage: String?
    val userAgent: String?
}

package ir.amirab.downloader.downloaditem.ytdlp

import arrow.core.Option
import arrow.core.getOrElse
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ytdlp")
data class YtdlpDownloadCredentials(
    override val link: String,
    override val downloadPage: String? = null,
) : IDownloadCredentials {
    override fun validateCredentials() {
        require(link.isNotBlank()) { "url is not valid" }
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
}

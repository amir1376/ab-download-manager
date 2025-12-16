package ir.amirab.downloader.downloaditem

import arrow.core.None
import arrow.core.Option
import arrow.core.none


interface IDownloadCredentials {
    val link: String
    val downloadPage: String?

    fun validateCredentials()

    fun copy(
        link: Option<String> = None,
        downloadPage: Option<String?> = None,
    ): IDownloadCredentials
}

package ir.amirab.downloader.downloaditem.http

import ir.amirab.downloader.downloaditem.IDownloadCredentials

interface IHttpBasedDownloadCredentials : IDownloadCredentials {
    val headers: Map<String, String>?
    val username: String?
    val password: String?
    val userAgent: String?
}

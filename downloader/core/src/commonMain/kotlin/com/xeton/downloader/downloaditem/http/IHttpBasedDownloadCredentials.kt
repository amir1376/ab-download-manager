package com.xeton.downloader.downloaditem.http

import com.xeton.downloader.downloaditem.IDownloadCredentials

interface IHttpBasedDownloadCredentials : IDownloadCredentials {
    val headers: Map<String, String>?
    val username: String?
    val password: String?
    val userAgent: String?
}

package com.abdownloadmanager.shared.downloaderinui

import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials

interface LinkCheckerFactory<
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TDownloadSize : DownloadSize,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo, TDownloadSize>,
        > {
    fun createLinkChecker(
        initialCredentials: TCredentials
    ): TLinkChecker
}

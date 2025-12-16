package com.abdownloadmanager.shared.downloaderinui

import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials

interface LinkCheckerFactory<
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo>,
        > {
    fun createLinkChecker(
        initialCredentials: TCredentials
    ): TLinkChecker
}

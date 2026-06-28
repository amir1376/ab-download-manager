package com.abdownloadmanager.shared.downloaderinui

import com.xeton.downloader.connection.IResponseInfo
import com.xeton.downloader.downloaditem.IDownloadCredentials

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

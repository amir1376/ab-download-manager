package com.abdownloadmanager.shared.downloaderinui.add

import com.abdownloadmanager.shared.downloaderinui.DownloadSize
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.util.DownloadSystem
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import kotlinx.coroutines.CoroutineScope

interface NewDownloadInputsFactory<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfoType : IResponseInfo,
        TDownloadSize : DownloadSize,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfoType, TDownloadSize>,
        TNewDownloadInputs : NewDownloadInputs<
                TDownloadItem,
                TCredentials,
                TResponseInfoType,
                TDownloadSize,
                TLinkChecker,
                >
        > {
    fun createNewDownloadInputs(
        initialCredentials: TCredentials,
        initialFolder: String,
        initialName: String,
        downloadSystem: DownloadSystem,
        scope: CoroutineScope
    ): TNewDownloadInputs
}

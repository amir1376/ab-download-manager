package com.abdownloadmanager.shared.downloaderinui.edit

import com.abdownloadmanager.shared.downloaderinui.CredentialAndItemMapper
import com.abdownloadmanager.shared.downloaderinui.LinkChecker
import com.abdownloadmanager.shared.downloaderinui.TADownloaderInUI
import com.abdownloadmanager.shared.downloaderinui.add.NewDownloadInputs
import com.abdownloadmanager.shared.downloaderinui.http.HttpCredentialsToItemMapper
import com.abdownloadmanager.shared.downloaderinui.http.add.HttpLinkChecker
import com.abdownloadmanager.shared.downloaderinui.http.edit.EditDownloadChecker
import ir.amirab.downloader.connection.IResponseInfo
import ir.amirab.downloader.connection.response.HttpResponseInfo
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

interface EditDownloadInputsFactory<
        TDownloadItem : IDownloadItem,
        TCredentials : IDownloadCredentials,
        TResponseInfo : IResponseInfo,
        TLinkChecker : LinkChecker<TCredentials, TResponseInfo>,
        TCredentialsToItemMapper : CredentialAndItemMapper<TCredentials, TDownloadItem>,
        TEditDownloadInputs : EditDownloadInputs<TDownloadItem, TCredentials, TResponseInfo, TLinkChecker, TCredentialsToItemMapper>
        > {
    fun createEditDownloadInputs(
        currentDownloadItem: MutableStateFlow<TDownloadItem>,
        editedDownloadItem: MutableStateFlow<TDownloadItem>,
        conflictDetector: DownloadConflictDetector,
        scope: CoroutineScope,
    ): TEditDownloadInputs
}



package com.abdownloadmanager.shared.downloaderinui

import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem

/**
 * the resulting value is a copied object so implementations doesn't have side effects
 */
interface CredentialAndItemMapper<TCredentials : IDownloadCredentials, TDownloadItem : IDownloadItem> {
    fun itemToCredentials(item: TDownloadItem): TCredentials
    fun appliedCredentialsToItem(item: TDownloadItem, credentials: TCredentials): TDownloadItem

    // I believe that these two are redundant it needs to be improved
    fun itemWithEditedName(item: TDownloadItem, name: String): TDownloadItem
    fun credentialsWithEditedLink(credentials: TCredentials, link: String): TCredentials
}

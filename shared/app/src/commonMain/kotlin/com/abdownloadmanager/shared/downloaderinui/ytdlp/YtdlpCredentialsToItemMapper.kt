package com.abdownloadmanager.shared.downloaderinui.ytdlp

import com.abdownloadmanager.shared.downloaderinui.CredentialAndItemMapper
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadCredentials
import ir.amirab.downloader.downloaditem.ytdlp.YtdlpDownloadItem

object YtdlpCredentialsToItemMapper : CredentialAndItemMapper<YtdlpDownloadCredentials, YtdlpDownloadItem> {
    override fun itemToCredentials(item: YtdlpDownloadItem): YtdlpDownloadCredentials {
        return YtdlpDownloadCredentials(item.link, item.downloadPage)
    }

    override fun appliedCredentialsToItem(
        item: YtdlpDownloadItem,
        credentials: YtdlpDownloadCredentials
    ): YtdlpDownloadItem {
        return item.copy(
            link = credentials.link,
            downloadPage = credentials.downloadPage
        )
    }

    override fun itemWithEditedName(item: YtdlpDownloadItem, name: String): YtdlpDownloadItem {
        return item.copy(name = name)
    }

    override fun credentialsWithEditedLink(
        credentials: YtdlpDownloadCredentials,
        link: String
    ): YtdlpDownloadCredentials {
        return credentials.copy(link = link)
    }
}

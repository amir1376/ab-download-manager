package com.abdownloadmanager.shared.downloaderinui.torrent

import com.abdownloadmanager.shared.downloaderinui.CredentialAndItemMapper
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadCredentials
import ir.amirab.downloader.downloaditem.torrent.TorrentDownloadItem

class TorrentItemToCredentialMapper : CredentialAndItemMapper<
        TorrentDownloadCredentials, TorrentDownloadItem> {
    override fun itemToCredentials(item: TorrentDownloadItem): TorrentDownloadCredentials {
        return TorrentDownloadCredentials(
            link = item.link,
            downloadPage = item.downloadPage,
            headers = item.headers,
            username = item.username,
            password = item.password,
            userAgent = item.userAgent,
        )
    }

    override fun appliedCredentialsToItem(
        item: TorrentDownloadItem,
        credentials: TorrentDownloadCredentials
    ): TorrentDownloadItem {
        return item.copy().withCredentials(credentials)
    }

    override fun itemWithEditedName(
        item: TorrentDownloadItem,
        name: String
    ): TorrentDownloadItem {
        return item.copy(name = name)
    }

    override fun credentialsWithEditedLink(
        credentials: TorrentDownloadCredentials,
        link: String
    ): TorrentDownloadCredentials {
        return credentials.copy(link = link)
    }

}

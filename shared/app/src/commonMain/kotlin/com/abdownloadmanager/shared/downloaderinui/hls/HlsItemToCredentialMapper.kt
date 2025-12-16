package com.abdownloadmanager.shared.downloaderinui.hls

import com.abdownloadmanager.shared.downloaderinui.CredentialAndItemMapper
import ir.amirab.downloader.downloaditem.hls.HLSDownloadCredentials
import ir.amirab.downloader.downloaditem.hls.HLSDownloadItem

class HlsItemToCredentialMapper : CredentialAndItemMapper<
        HLSDownloadCredentials, HLSDownloadItem> {
    override fun itemToCredentials(item: HLSDownloadItem): HLSDownloadCredentials {
        return HLSDownloadCredentials(
            link = item.link,
            downloadPage = item.downloadPage,
            headers = item.headers,
            username = item.username,
            password = item.password,
            userAgent = item.userAgent,
        )
    }

    override fun appliedCredentialsToItem(
        item: HLSDownloadItem,
        credentials: HLSDownloadCredentials
    ): HLSDownloadItem {
        return item.copy().withCredentials(credentials)
    }

    override fun itemWithEditedName(
        item: HLSDownloadItem,
        name: String
    ): HLSDownloadItem {
        return item.copy(name = name)
    }

    override fun credentialsWithEditedLink(
        credentials: HLSDownloadCredentials,
        link: String
    ): HLSDownloadCredentials {
        return credentials.copy(link = link)
    }

}
